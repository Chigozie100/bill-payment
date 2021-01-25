package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerConstants.SUCCESSFUL;

@Slf4j
@Service
public class QuickTellerService implements IThirdPartyService {

    private AppConfig appConfig;
    private QuickTellerFeignClient feignClient;
    private static Map<String, BillerDetail> billerDetailMap = new HashMap<>();
    private static final  String INVALID_BILLER_MESSAGE = "Invalid Biller provided";
    private static final  String SELECTED_ITEM_PARAM_NAME = "Item";

    public QuickTellerService(AppConfig appConfig, QuickTellerFeignClient feignClient) {
        this.appConfig = appConfig;
        this.feignClient = feignClient;
    }

    private String getAuthorisation(){
        return "InterswitchAuth "+Base64.getEncoder().encodeToString(appConfig.getQuickteller().getClientId().getBytes());
    }

    private String getNonce(){
        return UUID.randomUUID().toString();
    }

    private String getTimeStamp(){
        return String.valueOf(System.currentTimeMillis()).substring(0, 10);
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
        getAllBillersResponse.getBillers().forEach(billerDetail -> billerDetailMap.put(billerDetail.getBillerid(), billerDetail));
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
    public CustomerValidationResponse validateCustomerValidationFormByBiller(CustomerValidationRequest request) throws ThirdPartyIntegrationException {
        //get the customerIds and send for validation
        Optional<QuickTellerCustomerValidationResponse> quickTellerCustomerValidationResponseOptional = Optional.empty();
        try {
            BillerDetail billerDetail = billerDetailMap.get(request.getBillerId());
            if (Objects.isNull(billerDetail)){
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, INVALID_BILLER_MESSAGE);
            }

            QuickTellerCustomerValidationRequest validationRequest = generateValidationRequest(request, new QuickTellerCustomerValidationRequest(), billerDetail);
            String nonce = getNonce();
            String timeStamp = getTimeStamp();
            quickTellerCustomerValidationResponseOptional = Optional.of(feignClient.validateCustomerInfo(validationRequest, getAuthorisation(), getSignature(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getCustomerValidationUrl(), timeStamp, nonce), nonce, timeStamp, getSignatureMethod(), appConfig.getQuickteller().getTerminalId()));
        } catch (FeignException e) {
            log.error("Unable to process customer validation against interswitch ", e);
        }

        QuickTellerCustomerValidationResponse response = quickTellerCustomerValidationResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to validate customer details"));
        return getCustomerValidationResponse(request.getCategoryId(), request.getBillerId(), response);
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {

        Optional<SendPaymentAdviceResponse> sendPaymentAdviceResponseOptional = Optional.empty();
        try {
            BillerDetail billerDetail = billerDetailMap.get(request.getBillerId());
            if (Objects.isNull(billerDetail)){
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, INVALID_BILLER_MESSAGE);
            }

            String nonce = getNonce();
            String timeStamp = getTimeStamp();
            SendPaymentAdviceRequest sendPaymentAdviceRequest = generateRequest(request, billerDetail, timeStamp);
            sendPaymentAdviceResponseOptional = Optional.of(feignClient.sendPaymentAdvice(sendPaymentAdviceRequest, getAuthorisation(), getSignature(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getSendPaymentAdviceUrl(), timeStamp, nonce), nonce, timeStamp, getSignatureMethod(), appConfig.getQuickteller().getTerminalId()));
        } catch (FeignException e) {
            log.error("Unable to process payment against interswitch ", e);
        }

        SendPaymentAdviceResponse paymentAdviceResponse = sendPaymentAdviceResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to process payment"));

        if (SUCCESSFUL.equals(paymentAdviceResponse.getResponseCode())){
            return getPaymentResponse(paymentAdviceResponse);
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, paymentAdviceResponse.getResponseMessage());
    }

    private PaymentResponse getPaymentResponse(SendPaymentAdviceResponse paymentAdviceResponse){
        PaymentResponse paymentResponse = new PaymentResponse();

        if(CommonUtils.isNonEmpty(paymentAdviceResponse.getMiscData())){
            paymentResponse.getData().add(new ParamNameValue("miscData", paymentAdviceResponse.getMiscData()));
        }

        if(CommonUtils.isNonEmpty(paymentAdviceResponse.getRechargePIN())){
            paymentResponse.getData().add(new ParamNameValue("rechargePin", paymentAdviceResponse.getRechargePIN()));
        }

        paymentResponse.getData().add(new ParamNameValue("transactionRef", paymentAdviceResponse.getTransactionRef()));

        return paymentResponse;
    }

    private SendPaymentAdviceRequest generateRequest(PaymentRequest paymentRequest, BillerDetail billerDetail, String timeStamp){
        QuickTellerUserParam userParam = getUserParam(paymentRequest.getData(), billerDetail);
        SendPaymentAdviceRequest request = new SendPaymentAdviceRequest();
        request.setAmount(getAmountInKobo(paymentRequest.getAmount()));
        request.setCustomerId(userParam.getCustomerId1());
        request.setPaymentCode(userParam.getPaymentCode());
        request.setTerminalId(appConfig.getQuickteller().getTerminalId());
        request.setRequestReference(appConfig.getQuickteller().getTransactionRefCode()+timeStamp);
        return request;
    }

    private QuickTellerCustomerValidationRequest generateValidationRequest(CustomerValidationRequest request,
                                                                           QuickTellerCustomerValidationRequest validationRequest,
                                                                           BillerDetail billerDetail){

        QuickTellerUserParam userParam = getUserParam(request.getData(), billerDetail);
        validationRequest.getCustomers().add(new ValidationRequest(userParam.getCustomerId1(), userParam.getPaymentCode()));
//        validationRequest.getCustomers().add(new ValidationRequest(customerId2, paymentCode));
        return validationRequest;
    }

    private QuickTellerUserParam getUserParam(List<ParamNameValue> data, BillerDetail billerDetail){
        String customerId1 = Strings.EMPTY;
        String customerId2 = Strings.EMPTY;
        String paymentCode = Strings.EMPTY;

        for (ParamNameValue paramNameValue : data) {
            if (paramNameValue.getName().equals(billerDetail.getCustomerfield1())){
                customerId1 = paramNameValue.getValue();
            }

            if (paramNameValue.getName().equals(billerDetail.getCustomerfield2())){
                customerId2 = paramNameValue.getValue();
            }

            if (paramNameValue.getName().equals(SELECTED_ITEM_PARAM_NAME)){
                paymentCode = paramNameValue.getValue();
            }
        }

        return new QuickTellerUserParam(customerId1, customerId2, paymentCode);
    }

    private CustomerValidationResponse getCustomerValidationResponse(String categoryId, String billerId, QuickTellerCustomerValidationResponse validationResponse) throws ThirdPartyIntegrationException {
        CustomerValidationResponse customerValidationResponse = new CustomerValidationResponse(categoryId, billerId);
        ValidationResponse validationResponse1 = validationResponse.getCustomers().get(0);
        if(validationResponse1.getResponseCode().equals(SUCCESSFUL)) {
            customerValidationResponse.getData().add(new ParamNameValue("Amount", getCustomerValidationAmount(validationResponse1.getAmount(), validationResponse1.getAmountType())));
            customerValidationResponse.getData().add(new ParamNameValue("full Name", validationResponse1.getFullName()));
            return customerValidationResponse;
        }
        log.error("Response Message from Interswitch is {}", validationResponse1.getResponseDescription());
        throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unable to validate customerId provided");
    }

    private String getCustomerValidationAmount(String amount, String amountType){

        switch (amountType){

            case "2":
                return getAmountInNaira(amount).add(BigDecimal.ONE).toString();

            case "4":
                return getAmountInNaira(amount).subtract(BigDecimal.ONE).toString();
            
            case "1":
            case "3":
            case "5":
                return getAmountInNaira(amount).toString();

            default:
            case "0":
                return "";
        }
    }

    private BigDecimal getAmountInNaira(String amount){
        return new BigDecimal(amount).divide(BigDecimal.valueOf(100));
    }

    private String getAmountInKobo(String amount){
        return new BigDecimal(amount).multiply(BigDecimal.valueOf(100)).toString();
    }

    private PaymentItemsResponse getPaymentItemResponse(String categoryId, String billerId, GetBillerPaymentItemResponse getBillerPaymentItemResponse) throws ThirdPartyIntegrationException {
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);

        BillerDetail billerDetail = billerDetailMap.get(billerId);
        if (Objects.isNull(billerDetail)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, INVALID_BILLER_MESSAGE);
        }

        paymentItemsResponse.getItems().add(new Item(billerDetail.getCustomerfield1()));
        paymentItemsResponse.getItems().add(new Item(billerDetail.getCustomerfield2()));
        paymentItemsResponse.setIsValidationRequired(true);
        Item item = new Item(SELECTED_ITEM_PARAM_NAME);

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
        BigDecimal bigDecimalAmount = CommonUtils.isEmpty(amount) ? BigDecimal.ZERO : getAmountInNaira(amount);
        BigDecimal bigDecimalItemFee = CommonUtils.isEmpty(itemFee) ? BigDecimal.ZERO : getAmountInNaira(itemFee);
        return bigDecimalAmount.add(bigDecimalItemFee).setScale(2, RoundingMode.UNNECESSARY).toString();
    }
}

class QuickTellerConstants {
    static final String AUTHORIZATION = "Authorization";
    static final String SIGNATURE = "Signature";
    static final String NONCE = "Nonce";
    static final String TIMESTAMP = "Timestamp";
    static final String SIGNATURE_METHOD = "SignatureMethod";
    static final String TERMINAL_ID = "TerminalId";
    static final String SUCCESSFUL = "9000";
}
