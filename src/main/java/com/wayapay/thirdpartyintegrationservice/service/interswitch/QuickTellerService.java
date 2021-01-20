package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class QuickTellerService implements IThirdPartyService {

    private AppConfig appConfig;
    private QuickTellerFeignClient feignClient;
    private static Map<String, CategoryDetail> categoryDetailMap = new HashMap<>();
    private static Map<String, BillerDetail> billerDetailMap = new HashMap<>();

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

    @Override
    public List<String> getCategory() throws ThirdPartyIntegrationException {

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

        categoryResponseOptional.get().getCategorys().forEach(categoryDetail -> categoryDetailMap.put(categoryDetail.getCategoryname(), categoryDetail));
        return new ArrayList<>(categoryDetailMap.keySet());
    }

    @Override
    public List<String> getAllBillersByCategory(String category) throws ThirdPartyIntegrationException {

        Optional<GetAllBillersByCategoryResponse> billersByCategoryResponseOptional = Optional.empty();

        try {
            String nonce = getNonce();
            String timeStamp = getTimeStamp();
            billersByCategoryResponseOptional = Optional.of(feignClient.getAllBillersByCategory(categoryDetailMap.getOrDefault(category, new CategoryDetail()).getCategoryid(), getAuthorisation(), getSignature(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getBillerCategoryUrl(), timeStamp, nonce), nonce, timeStamp, getSignatureMethod()));
        } catch (FeignException e) {
            log.error("Unable to fetch billers by category => {} from interswitch ", categoryDetailMap.get(category), e);
        }

        if (!billersByCategoryResponseOptional.isPresent()){
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch billers");
        }

        GetAllBillersByCategoryResponse getAllBillersByCategoryResponse = billersByCategoryResponseOptional.get();
        getAllBillersByCategoryResponse.getBillers().forEach(billerDetail -> billerDetailMap.put(billerDetail.getBillername(), billerDetail));
        return new ArrayList<>(billerDetailMap.keySet());
    }

    @Override
    public List<String> getCustomerValidationFormByBiller(String billerName) throws ThirdPartyIntegrationException {
        return new ArrayList<>();
    }

    @Override
    public void validateCustomerValidationFormByBiller() throws ThirdPartyIntegrationException {

    }

    @Override
    public void processPayment() throws ThirdPartyIntegrationException {

    }
}
