package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.dto.BillerResponse;
import com.wayapay.thirdpartyintegrationservice.dto.Item;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentItemsResponse;
import com.wayapay.thirdpartyintegrationservice.dto.SubItem;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuickTellerService implements IThirdPartyService {

    private AppConfig appConfig;
    private QuickTellerFeignClient feignClient;

    public QuickTellerService(AppConfig appConfig, QuickTellerFeignClient feignClient) {
        this.appConfig = appConfig;
        this.feignClient = feignClient;
    }

    private String getAccessToken() throws ThirdPartyIntegrationException {
        Optional<AuthResponse> optionalAuthResponse = Optional.empty();
        try {
            optionalAuthResponse = Optional.of(feignClient.getAuthToken("Basic " + Base64.getEncoder().encodeToString((appConfig.getQuickteller().getClientId() + ":" + appConfig.getQuickteller().getSecret()).getBytes())));
        } catch (FeignException e) {
            log.error("Unable to fetch auth token => ", e);
        }
        if (!optionalAuthResponse.isPresent()){
            log.error("Unable to ");
            throw new ThirdPartyIntegrationException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get auth token");
        }
        return optionalAuthResponse.get().getAccess_token();
    }

    private String getAuthorisation(){
        return "InterswitchAuth "+Base64.getEncoder().encodeToString(appConfig.getQuickteller().getClientId().getBytes());
    }

    private String getNonce(){
        return UUID.randomUUID().toString();
    }

    private String getTimeStamp(){
        return String.valueOf(System.currentTimeMillis());
    }

    private String getSignature(HttpMethod httpMethod, String url, String timeStamp, String nonce){
        String signatureCipher = httpMethod.name() + "&" + url + "&" + timeStamp + "&" + nonce + "&" + appConfig.getQuickteller().getClientId() + "&" + appConfig.getQuickteller().getSecret();
        return Base64.getEncoder().encodeToString(DigestUtils.sha1Hex(signatureCipher).getBytes());
    }

    private String getSignatureMethod(){
        return "SHA1";
    }

    //todo cache the request and response
    @Override
    public List<com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse> getCategory() throws ThirdPartyIntegrationException {

        Optional<CategoryResponse> categoryResponseOptional = Optional.empty();

        try {
            String nonce = getNonce();
            String timeStamp = getTimeStamp();
            categoryResponseOptional = Optional.of(feignClient.getCategory(getAuthorisation(), getSignature(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getBillerCategoryUrl(), timeStamp, nonce), nonce, timeStamp, getSignatureMethod()));
        } catch (FeignException e) {
            log.error("Unable to fetch categories from Interswitch", e);
        }

        if (!categoryResponseOptional.isPresent()) {
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch categories");
        }

        return categoryResponseOptional.get().getCategorys().parallelStream().map(categoryDetail -> new com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse(categoryDetail.getCategoryid(), categoryDetail.getCategoryname())).collect(Collectors.toList());
    }

    //todo cache the request and response
    @Override
    public List<BillerResponse> getAllBillersByCategory(String categoryId) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(categoryId)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category Id provided");
        }

        Optional<GetAllBillersResponse> billersResponseOptional = Optional.empty();
        try {
            String nonce = getNonce();
            String timeStamp = getTimeStamp();
            billersResponseOptional = Optional.of(feignClient.getAllBillers(getAuthorisation(), getSignature(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getBillerCategoryUrl(), timeStamp, nonce), nonce, timeStamp, getSignatureMethod()));
        } catch (FeignException e) {
            log.error("Unable to fetch billers by category => {} from interswitch ", categoryId, e);
        }

        GetAllBillersResponse getAllBillersResponse = billersResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch billers"));
        return getAllBillersResponse.getBillers().parallelStream().filter(billerDetail -> billerDetail.getCategoryid().equals(categoryId)).map(billerDetail -> new BillerResponse(billerDetail.getBillerid(), billerDetail.getBillername(), billerDetail.getCategoryid())).collect(Collectors.toList());
    }

    @Override
    public PaymentItemsResponse getCustomerValidationFormByBiller(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(categoryId) || CommonUtils.isEmpty(billerId)) {
            log.error("categoryId => {} or billerid => {} is empty/null ", categoryId, billerId);
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category or biller provided");
        }

        Optional<GetBillerPaymentItemResponse> billerPaymentItemsResponseOptional = Optional.empty();
        try {
            String nonce = getNonce();
            String timeStamp = getTimeStamp();
            billerPaymentItemsResponseOptional = Optional.of(feignClient.getBillerPaymentItems(billerId, getAuthorisation(), getSignature(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getBillerPaymentItemUrl().replace("{billerId}", billerId), timeStamp, nonce), nonce, timeStamp, getSignatureMethod()));
        } catch (FeignException e) {
            log.error("Unable to fetch billers paymentitems, billerId is => {} from interswitch ", billerId, e);
        }

        GetBillerPaymentItemResponse getBillerPaymentItemResponse = billerPaymentItemsResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch biller's payment items"));
        return getPaymentItemResponse(categoryId, billerId, getBillerPaymentItemResponse);
    }

    @Override
    public void validateCustomerValidationFormByBiller() throws ThirdPartyIntegrationException {

    }

    @Override
    public void processPayment() throws ThirdPartyIntegrationException {

    }

    private PaymentItemsResponse getPaymentItemResponse(String categoryId, String billerId, GetBillerPaymentItemResponse getBillerPaymentItemResponse){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        Item item = new Item("item");

        getBillerPaymentItemResponse.getPaymentitems().forEach(paymentItem -> {
            if (paymentItem.getIsAmountFixed()) {
                item.setIsAccountFixed(paymentItem.getIsAmountFixed());
            }
            String amount = getAmount(paymentItem.getAmount(), paymentItem.getItemFee());
            item.getSubItems().add(new SubItem(paymentItem.getPaymentCode(), paymentItem.getPaymentitemname(), amount, amount));
        });
        paymentItemsResponse.getItems().add(item);
        return paymentItemsResponse;
    }

    private String getAmount(String amount, String itemFee){
        BigDecimal bigDecimalAmount = CommonUtils.isEmpty(amount) ? BigDecimal.ZERO : new BigDecimal(amount);
        BigDecimal bigDecimalItemFee = CommonUtils.isEmpty(itemFee) ? BigDecimal.ZERO : new BigDecimal(itemFee);
        return bigDecimalAmount.add(bigDecimalItemFee).setScale(2, RoundingMode.UNNECESSARY).toString();
    }
}

class QuickTellerConstants {
    static final String AUTHORIZATION = "Authorization";
    static final String SIGNATURE = "Signature";
    static final String NONCE = "Nonce";
    static final String TIMESTAMP = "Timestamp";
    static final String SIGNATURE_METHOD = "SignatureMethod";
}
