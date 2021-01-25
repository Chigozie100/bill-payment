package com.wayapay.thirdpartyintegrationservice.service.itex;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.wayapay.thirdpartyintegrationservice.service.itex.BillerName.*;

@Slf4j
@Service
public class ItexService implements IThirdPartyService {

    private ItexFeignClient feignClient;
    private AppConfig appConfig;
    private ItexUtil itexUtil;
    private List<SubItem> channelsSubList = new ArrayList<>();
    private List<SubItem> paymentMethodSubList = new ArrayList<>();
    private static final String AMOUNT = "amount";
    private static final String ACCOUNT = "account";
    private static final String ACCOUNT_TYPE = "accountType";
    private static final String METER_NO = "meterNo";
    private static final String CHANNEL = "channel";
    private static final String PRODUCT_CODE = "productCode";
    private static final String SUCCESS = "00";
    private static final String CUSTOMER_PHONE_NUMBER = "customerPhoneNumber";
    private static final String PAYMENT_METHOD = "paymentMethod";
    private static final String PHONE = "phone";
    private static final String REFERENCE = "reference";
    private static List<BillerResponse> categoryElectricity = Arrays.asList(new BillerResponse("ikedc", "IKEDC", ELECTRICITY), new BillerResponse("aedc", "AEDC", ELECTRICITY), new BillerResponse("phedc", "PHEDC", ELECTRICITY), new BillerResponse("eedc", "EEDC", ELECTRICITY), new BillerResponse("ibedc", "IBEDC", ELECTRICITY), new BillerResponse("ekedc", "EKEDC", ELECTRICITY), new BillerResponse("kedco", "KEDCO", ELECTRICITY));
    private static List<BillerResponse> categoryAirtime = Arrays.asList(new BillerResponse("mtnvtu", "MTN VTU", AIRTIME), new BillerResponse("9mobilevtu", "9MOBILE VTU", AIRTIME), new BillerResponse("glovtu", "GLO VTU", AIRTIME), new BillerResponse("glovot", "GLO VOT", AIRTIME), new BillerResponse("glovos", "GLO VOS", AIRTIME), new BillerResponse("airtelpin", "AIRTEL PIN", AIRTIME), new BillerResponse("airtelvtu", "AIRTEL VTU", AIRTIME));
    private static List<BillerResponse> categoryData = Arrays.asList(new BillerResponse( "mtndata","MTN DATA", DATA), new BillerResponse( "9mobiledata","9MOBILE DATA", DATA), new BillerResponse( "glodata","GLO DATA", DATA), new BillerResponse( "airteldata","AIRTEL DATA", DATA));
    private static List<BillerResponse> categoryCableTv = Arrays.asList(new BillerResponse("multichoice", "multichoice", CABLE_TV), new BillerResponse("startimes", "STARTIMES",CABLE_TV));
    private static List<BillerResponse> categoryInternet = Collections.singletonList(new BillerResponse("smile", "SMILE", INTERNET));
    private static List<BillerResponse> categoryRemita = Collections.singletonList(new BillerResponse(REMITA, REMITA, REMITA));
    private static List<BillerResponse> categoryLcc = Collections.singletonList(new BillerResponse(LCC, LCC, LCC));

    public ItexService(ItexFeignClient feignClient, AppConfig appConfig, ItexUtil itexUtil) {
        this.feignClient = feignClient;
        this.appConfig = appConfig;
        this.itexUtil = itexUtil;
    }

    private Optional<String> getAuthApiToken() throws ThirdPartyIntegrationException {
        try {
            AuthApiTokenResponse authApiTokenResponse = feignClient.getAuthApiToken(new AuthApiTokenRequest(appConfig.getItex().getWalletId(), appConfig.getItex().getUsername(), appConfig.getItex().getPassword(), appConfig.getItex().getUniqueApiIdentifier()));
            if(SUCCESS.equals(authApiTokenResponse.getResponseCode())
                && !Objects.isNull(authApiTokenResponse.getData())
                && SUCCESS.equals(authApiTokenResponse.getData().getResponseCode())){
                return Optional.of(authApiTokenResponse.getData().getToken());
            }
            return Optional.empty();
        } catch (FeignException e) {
            log.error("Unable to generate authApiToken ", e);
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private Optional<String> generateEncryptedPin() throws ThirdPartyIntegrationException {
        try {
            EncryptedPinResponse encryptedPinResponse = feignClient.generateEncryptedPin(new EncryptedPinRequest(appConfig.getItex().getWalletId(), appConfig.getItex().getUsername(), appConfig.getItex().getPassword(), appConfig.getItex().getPayVicePin()));
            if(SUCCESS.equals(encryptedPinResponse.getResponseCode())
                    && !Objects.isNull(encryptedPinResponse.getData())
                    && SUCCESS.equals(encryptedPinResponse.getData().getResponseCode())){
                return Optional.of(encryptedPinResponse.getData().getPin());
            }
            return Optional.empty();
        } catch (FeignException e) {
            log.error("Unable to generate EncryptedPin", e);
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private Optional<String> generateSignature(String jsonRequestAsString){
        try {
            return Optional.of(itexUtil.generateHmac256(jsonRequestAsString, appConfig.getItex().getHmacsha256key()));
        } catch (InvalidKeyException e) {
            log.error("InvalidKeyException: ", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException: ", e);
        }

        return Optional.empty();
    }

    @Override
    public List<CategoryResponse> getCategory() {
        return Arrays.asList(new CategoryResponse(ELECTRICITY, ELECTRICITY), new CategoryResponse(AIRTIME, AIRTIME), new CategoryResponse(DATA, DATA), new CategoryResponse(CABLE_TV, CABLE_TV), new CategoryResponse(INTERNET, INTERNET), new CategoryResponse(REMITA, REMITA), new CategoryResponse(LCC, LCC));
    }

    //todo cache the request and response
    @Override
    public List<BillerResponse> getAllBillersByCategory(String categoryId) throws ThirdPartyIntegrationException {

        switch (categoryId){
            case ELECTRICITY:
                return categoryElectricity;

            case AIRTIME:
                return categoryAirtime;

            case DATA:
                return categoryData;

            case CABLE_TV:
                return categoryCableTv;

            case INTERNET:
                return categoryInternet;

            case REMITA:
                return categoryRemita;

            case LCC:
                return categoryLcc;

            default:
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unknown category Id provided");
        }

    }

    @Override
    public PaymentItemsResponse getCustomerValidationFormByBiller(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(categoryId) || CommonUtils.isEmpty(billerId)){
            log.error("categoryId => {} or billerId => {} is empty/null ", categoryId, billerId);
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category or biller provided");
        }

        if (categoryElectricity.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getElectricityPaymentItems(billerId, categoryId);
        } else if (categoryAirtime.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getAirtimePaymentItems(billerId, categoryId);
        } else if (categoryData.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getDataPaymentItems(billerId, categoryId);
        } else if (categoryInternet.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getInternetPaymentItems(billerId, categoryId);
        } else if (categoryCableTv.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getCableTvPaymentItems(billerId, categoryId);
        } else if (categoryRemita.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getRemitaPaymentItems(billerId, categoryId);
        } else if (categoryLcc.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(billerId))){
            return getLCCPaymentItems(billerId, categoryId);
        } else {
           throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unknown biller name provided");
        }
    }

    @Override
    public CustomerValidationResponse validateCustomerValidationFormByBiller(CustomerValidationRequest request) throws ThirdPartyIntegrationException {

        //for cableTv and Internet, the validationResponse should contain the bouquets or bundles

        if (CommonUtils.isEmpty(request.getCategoryId()) || CommonUtils.isEmpty(request.getBillerId())){
            log.error("categoryId => {} or billerId => {} is empty/null ", request.getCategoryId(), request.getBillerId());
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category or biller provided");
        }

        if (categoryElectricity.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return validateElectricity(request);
        } else if (categoryAirtime.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            //Validation is not required
            return new CustomerValidationResponse();
        } else if (categoryData.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return validateData(request);
        } else if (categoryInternet.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return validateInternet(request);
        } else if (categoryCableTv.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return new CustomerValidationResponse();
//            return getCableTvPaymentItems(billerId, categoryId);
        } else if (categoryRemita.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return new CustomerValidationResponse();
//            return getRemitaPaymentItems(billerId, categoryId);
        } else if (categoryLcc.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return new CustomerValidationResponse();
//            return getLCCPaymentItems(billerId, categoryId);
        } else {
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unknown biller name provided");
        }

    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {

        //ensure that the MONEY to be paid is secured alongside the FEE
        //Initiate Payment
        //#Async : Confirm Payment Status by making a Transaction status, if not successfull, then reverse the MONEY and the FEE.

        if (categoryElectricity.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return paymentElectricity(request, transactionId);
        } else if (categoryAirtime.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return paymentAirtime(request, transactionId);
        } else if (categoryData.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return paymentData(request, transactionId);
        } else if (categoryInternet.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return paymentInternet(request, transactionId);
        } else if (categoryCableTv.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return new PaymentResponse();
//            return getCableTvPaymentItems(billerId, categoryId);
        } else if (categoryRemita.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return new PaymentResponse();
//            return getRemitaPaymentItems(billerId, categoryId);
        } else if (categoryLcc.parallelStream().anyMatch(billerResponse -> billerResponse.getBillerId().equals(request.getBillerId()))){
            return new PaymentResponse();
//            return getLCCPaymentItems(billerId, categoryId);
        } else {
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unknown biller name provided");
        }

    }

    private CustomerValidationResponse validateInternet(CustomerValidationRequest request) throws ThirdPartyIntegrationException {
        InternetValidationRequest validationRequest = generateInternetValidationRequest(request);
        Optional<InternetValidationResponse> internetValidationResponseOptional = Optional.empty();
        try {
            internetValidationResponseOptional = Optional.of(feignClient.internetValidation(validationRequest, getAuthApiToken().orElseGet(() -> Strings.EMPTY), generateSignature(CommonUtils.ObjectToJson(validationRequest).orElse(Strings.EMPTY)).orElse(Strings.EMPTY)));
        } catch (FeignException e) {
            log.error("Unable to validate customer internet detail via itex ", e);
        }
        InternetValidationResponse internetValidationResponse = internetValidationResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS.equals(internetValidationResponse.getResponseCode())
                && !Objects.isNull(internetValidationResponse.getData()) && SUCCESS.equals(internetValidationResponse.getData().getResponseCode())) {

            CustomerValidationResponse customerValidationResponse = new CustomerValidationResponse(request.getCategoryId(), request.getBillerId());
            customerValidationResponse.getData().add(new ParamNameValue(PRODUCT_CODE, internetValidationResponse.getData().getProductCode()));

            Item itemType = new Item("type");
            itemType.getSubItems().add(new SubItem("subscription"));
            itemType.getSubItems().add(new SubItem("topup"));

            customerValidationResponse.getItems().add(itemType);
            customerValidationResponse.getItems().add(new Item(PHONE));
            customerValidationResponse.getItems().add(getPaymentMethodAsItem());
            customerValidationResponse.getItems().add(getInternetBundles(validationRequest));
            return customerValidationResponse;
        }
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private Item getInternetBundles(InternetValidationRequest validationRequest) throws ThirdPartyIntegrationException {
        Optional<InternetBundleResponse> internetBundleResponseOptional = Optional.empty();
        try {
            internetBundleResponseOptional = Optional.of(feignClient.getInternetBundles(validationRequest, getAuthApiToken().orElseGet(() -> Strings.EMPTY), generateSignature(CommonUtils.ObjectToJson(validationRequest).orElse(Strings.EMPTY)).orElse(Strings.EMPTY)));
        } catch (FeignException e) {
            log.error("Unable to get customer internet bundles via itex ", e);
        }

        InternetBundleResponse internetBundleResponse = internetBundleResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS.equals(internetBundleResponse.getResponseCode())
                && !Objects.isNull(internetBundleResponse.getData()) && SUCCESS.equals(internetBundleResponse.getData().getResponseCode())) {
            Item itemBundles = new Item("bundle");
            internetBundleResponse.getData().getBundles().forEach(bundles -> itemBundles.getSubItems().add(new SubItem(bundles.getCode(), bundles.getCode(), getAmountInNaira(bundles.getPrice()), getAmountInNaira(bundles.getPrice()))));
            return itemBundles;
        }
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private CustomerValidationResponse validateData(CustomerValidationRequest request) throws ThirdPartyIntegrationException {
        DataValidationRequest validationRequest = generateDataValidationRequest(request);
        Optional<DataValidationResponse> dataValidationResponseOptional = Optional.empty();
        try {
            dataValidationResponseOptional = Optional.of(feignClient.dataValidation(validationRequest, getAuthApiToken().orElseGet(() -> Strings.EMPTY), generateSignature(CommonUtils.ObjectToJson(validationRequest).orElse(Strings.EMPTY)).orElse(Strings.EMPTY)));
        } catch (FeignException e) {
            log.error("Unable to validate customer data detail via itex ", e);
        }
        DataValidationResponse dataValidationResponse = dataValidationResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS.equals(dataValidationResponse.getResponseCode())
                && !Objects.isNull(dataValidationResponse.getData()) && SUCCESS.equals(dataValidationResponse.getData().getResponseCode())) {
            CustomerValidationResponse customerValidationResponse = new CustomerValidationResponse(request.getCategoryId(), request.getBillerId());
            customerValidationResponse.getData().add(new ParamNameValue(PRODUCT_CODE, dataValidationResponse.getData().getProductCode()));

            Item itemData = new Item("plan");
            itemData.setIsAccountFixed(Boolean.TRUE);
            dataValidationResponse.getData().getData().forEach(plan -> itemData.getSubItems().add(new SubItem(plan.getCode(), plan.getDescription(), plan.getAmount(), plan.getAmount())));
            customerValidationResponse.getItems().add(itemData);
            customerValidationResponse.getItems().add(getPaymentMethodAsItem());
            return customerValidationResponse;
        }
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private CustomerValidationResponse validateElectricity(CustomerValidationRequest request) throws ThirdPartyIntegrationException {
        ElectricityValidationRequest validationRequest = generateElectricityValidationRequest(request);
        Optional<ElectricityValidationResponse> electricityValidationResponseOptional = Optional.empty();
        try {
            electricityValidationResponseOptional = Optional.of(feignClient.electricityValidation(validationRequest, getAuthApiToken().orElseGet(() -> Strings.EMPTY), generateSignature(CommonUtils.ObjectToJson(validationRequest).orElse(Strings.EMPTY)).orElse(Strings.EMPTY)));
        } catch (FeignException e) {
            log.error("Unable to validate customer electricity detail via itex ", e);
        }
        ElectricityValidationResponse electricityValidationResponse = electricityValidationResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS.equals(electricityValidationResponse.getResponseCode())
                && !Objects.isNull(electricityValidationResponse.getData()) && SUCCESS.equals(electricityValidationResponse.getData().getResponseCode())) {
            CustomerValidationResponse customerValidationResponse = new CustomerValidationResponse(request.getCategoryId(), request.getBillerId());
            customerValidationResponse.getData().add(new ParamNameValue("AccountNumber", electricityValidationResponse.getData().getAccountNumber()));
            customerValidationResponse.getData().add(new ParamNameValue(METER_NO, electricityValidationResponse.getData().getMeterNumber()));
            customerValidationResponse.getData().add(new ParamNameValue(PRODUCT_CODE, electricityValidationResponse.getData().getProductCode()));
            customerValidationResponse.getData().add(new ParamNameValue(CUSTOMER_PHONE_NUMBER, electricityValidationResponse.getData().getPhone()));
            customerValidationResponse.getItems().add(getPaymentMethodAsItem());

            return customerValidationResponse;
        }
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private InternetValidationRequest generateInternetValidationRequest(CustomerValidationRequest request){
        InternetValidationRequest internetValidationRequest = new InternetValidationRequest();
        internetValidationRequest.setService(request.getBillerId());
        request.getData().forEach(paramNameValue -> {
            if (CHANNEL.equals(paramNameValue.getName())){ internetValidationRequest.setChannel(paramNameValue.getValue()); }
            if ("type".equals(paramNameValue.getName())){ internetValidationRequest.setType(paramNameValue.getValue()); }
            if (ACCOUNT.equals(paramNameValue.getName())){ internetValidationRequest.setAccount(paramNameValue.getValue()); }
        });
        return internetValidationRequest;
    }

    private DataValidationRequest generateDataValidationRequest(CustomerValidationRequest request){
        DataValidationRequest dataValidationRequest = new DataValidationRequest();
        dataValidationRequest.setService(request.getBillerId());
        request.getData().forEach(paramNameValue -> {
            if (CHANNEL.equals(paramNameValue.getName())){ dataValidationRequest.setChannel(paramNameValue.getValue()); }
        });
        return dataValidationRequest;
    }

    private ElectricityValidationRequest generateElectricityValidationRequest(CustomerValidationRequest request){

        ElectricityValidationRequest electricityValidationRequest = new ElectricityValidationRequest();
        request.getData().forEach(paramNameValue -> {
            if (ACCOUNT_TYPE.equals(paramNameValue.getName())){ electricityValidationRequest.setAccountType(paramNameValue.getValue()); }
            if (AMOUNT.equals(paramNameValue.getName())){ electricityValidationRequest.setAmount(paramNameValue.getValue()); }
            if (CHANNEL.equals(paramNameValue.getName())){ electricityValidationRequest.setChannel(paramNameValue.getValue()); }
            if (METER_NO.equals(paramNameValue.getName())){ electricityValidationRequest.setMeterNo(paramNameValue.getValue()); }
        });

        electricityValidationRequest.setService(request.getBillerId());
        return electricityValidationRequest;
    }

    private PaymentResponse paymentInternet(PaymentRequest paymentRequest, String transactionId) throws ThirdPartyIntegrationException {
        InternetPaymentRequest internetPaymentRequest = generateInternetPaymentRequest(paymentRequest, transactionId);
        Optional<InternetPaymentResponse> internetPaymentResponseOptional = Optional.empty();
        try {
            internetPaymentResponseOptional = Optional.of(feignClient.internetPayment(internetPaymentRequest, getAuthApiToken().orElseGet(() -> Strings.EMPTY), generateSignature(CommonUtils.ObjectToJson(internetPaymentRequest).orElse(Strings.EMPTY)).orElse(Strings.EMPTY)));
        } catch (FeignException e) {
            log.error("Unable to process customer internet payment via itex ", e);
        }

        InternetPaymentResponse internetPaymentResponse = internetPaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS.equals(internetPaymentResponse.getResponseCode())
                && !Objects.isNull(internetPaymentResponse.getData()) && SUCCESS.equals(internetPaymentResponse.getData().getResponseCode())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionID", internetPaymentResponse.getData().getTransactionID()));
            paymentResponse.getData().add(new ParamNameValue(REFERENCE, internetPaymentResponse.getData().getReference()));
            paymentResponse.getData().add(new ParamNameValue("bundle", internetPaymentResponse.getData().getBundle()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse paymentElectricity(PaymentRequest paymentRequest, String transactionId) throws ThirdPartyIntegrationException {
        ElectricityPaymentRequest electricityPaymentRequest = generateElectricityPaymentRequest(paymentRequest, transactionId);
        Optional<ElectricityPaymentResponse> electricityPaymentResponseOptional = Optional.empty();
        try {
            electricityPaymentResponseOptional = Optional.of(feignClient.electricityPayment(electricityPaymentRequest, getAuthApiToken().orElseGet(() -> Strings.EMPTY), generateSignature(CommonUtils.ObjectToJson(electricityPaymentRequest).orElse(Strings.EMPTY)).orElse(Strings.EMPTY)));
        } catch (FeignException e) {
            log.error("Unable to process customer electricity payment via itex ", e);
        }

        ElectricityPaymentResponse electricityPaymentResponse = electricityPaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS.equals(electricityPaymentResponse.getResponseCode())
                && !Objects.isNull(electricityPaymentResponse.getData()) && SUCCESS.equals(electricityPaymentResponse.getData().getResponseCode())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("token", electricityPaymentResponse.getData().getToken()));
            paymentResponse.getData().add(new ParamNameValue(REFERENCE, electricityPaymentResponse.getData().getReference()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse paymentAirtime(PaymentRequest paymentRequest, String transactionId) throws ThirdPartyIntegrationException {
        AirtimePaymentRequest airtimePaymentRequest = generateAirtimePaymentRequest(paymentRequest, transactionId);
        Optional<AirtimePaymentResponse> airtimePaymentResponseOptional = Optional.empty();
        try {
            airtimePaymentResponseOptional = Optional.of(feignClient.airtimePayment(airtimePaymentRequest, getAuthApiToken().orElseGet(() -> Strings.EMPTY), generateSignature(CommonUtils.ObjectToJson(airtimePaymentRequest).orElse(Strings.EMPTY)).orElse(Strings.EMPTY)));
        } catch (FeignException e) {
            log.error("Unable to process customer electricity payment via itex ", e);
        }

        AirtimePaymentResponse airtimePaymentResponse = airtimePaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS.equals(airtimePaymentResponse.getResponseCode())
                && !Objects.isNull(airtimePaymentResponse.getData()) && SUCCESS.equals(airtimePaymentResponse.getData().getResponseCode())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue(REFERENCE, airtimePaymentResponse.getData().getReference()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse paymentData(PaymentRequest paymentRequest, String transactionId) throws ThirdPartyIntegrationException {
        DataPaymentRequest dataPaymentRequest = generateDataPaymentRequest(paymentRequest, transactionId);
        Optional<DataPaymentResponse> dataPaymentResponseOptional = Optional.empty();
        try {
            dataPaymentResponseOptional = Optional.of(feignClient.dataPayment(dataPaymentRequest, getAuthApiToken().orElseGet(() -> Strings.EMPTY), generateSignature(CommonUtils.ObjectToJson(dataPaymentRequest).orElse(Strings.EMPTY)).orElse(Strings.EMPTY)));
        } catch (FeignException e) {
            log.error("Unable to process customer data payment via itex ", e);
        }

        DataPaymentResponse dataPaymentResponse = dataPaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS.equals(dataPaymentResponse.getResponseCode())
                && !Objects.isNull(dataPaymentResponse.getData()) && SUCCESS.equals(dataPaymentResponse.getData().getResponseCode())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue(REFERENCE, dataPaymentResponse.getData().getReference()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private InternetPaymentRequest generateInternetPaymentRequest(PaymentRequest paymentRequest, String transactionId) throws ThirdPartyIntegrationException {
        InternetPaymentRequest internetPaymentRequest = new InternetPaymentRequest();
        internetPaymentRequest.setClientReference(transactionId);
        internetPaymentRequest.setPin(generateEncryptedPin().orElseGet(() -> Strings.EMPTY));
        internetPaymentRequest.setService(paymentRequest.getBillerId());
        paymentRequest.getData().forEach(paramNameValue -> {
            if ("type".equals(paramNameValue.getName())){ internetPaymentRequest.setType(paramNameValue.getValue()); }
            if ("code".equals(paramNameValue.getName())){ internetPaymentRequest.setCode(paramNameValue.getValue()); }
            if (AMOUNT.equals(paramNameValue.getName())){ internetPaymentRequest.setAmount(paramNameValue.getValue()); }
            if (PAYMENT_METHOD.equals(paramNameValue.getName())){ internetPaymentRequest.setPaymentMethod(paramNameValue.getValue()); }
            if (PRODUCT_CODE.equals(paramNameValue.getName())){ internetPaymentRequest.setProductCode(paramNameValue.getValue()); }
            if (PHONE.equals(paramNameValue.getName())){ internetPaymentRequest.setPhone(paramNameValue.getValue()); }
        });
        return internetPaymentRequest;
    }

    private ElectricityPaymentRequest generateElectricityPaymentRequest(PaymentRequest paymentRequest, String transactionId) throws ThirdPartyIntegrationException {
        ElectricityPaymentRequest electricityPaymentRequest = new ElectricityPaymentRequest();
        electricityPaymentRequest.setClientReference(transactionId);
        electricityPaymentRequest.setPin(generateEncryptedPin().orElseGet(() -> Strings.EMPTY));
        electricityPaymentRequest.setService(paymentRequest.getBillerId());
        paymentRequest.getData().forEach(paramNameValue -> {
            if (CUSTOMER_PHONE_NUMBER.equals(paramNameValue.getName())){ electricityPaymentRequest.setCustomerPhoneNumber(paramNameValue.getValue()); }
            if (PAYMENT_METHOD.equals(paramNameValue.getName())){ electricityPaymentRequest.setPaymentMethod(paramNameValue.getValue()); }
            if (PRODUCT_CODE.equals(paramNameValue.getName())){ electricityPaymentRequest.setProductCode(paramNameValue.getValue()); }
        });
        return electricityPaymentRequest;
    }

    private AirtimePaymentRequest generateAirtimePaymentRequest(PaymentRequest paymentRequest, String transactionId) throws ThirdPartyIntegrationException {
        AirtimePaymentRequest airtimePaymentRequest = new AirtimePaymentRequest();
        airtimePaymentRequest.setClientReference(transactionId);
        airtimePaymentRequest.setPin(generateEncryptedPin().orElseGet(() -> Strings.EMPTY));
        airtimePaymentRequest.setService(paymentRequest.getBillerId());
        paymentRequest.getData().forEach(paramNameValue -> {
            if (PHONE.equals(paramNameValue.getName())){ airtimePaymentRequest.setPhone(paramNameValue.getValue()); }
            if (PAYMENT_METHOD.equals(paramNameValue.getName())){ airtimePaymentRequest.setPaymentMethod(paramNameValue.getValue()); }
            if (AMOUNT.equals(paramNameValue.getName())){ airtimePaymentRequest.setAmount(paramNameValue.getValue()); }
            if (CHANNEL.equals(paramNameValue.getName())){ airtimePaymentRequest.setChannel(paramNameValue.getValue()); }
        });
        return airtimePaymentRequest;
    }

    private DataPaymentRequest generateDataPaymentRequest(PaymentRequest paymentRequest, String transactionId) throws ThirdPartyIntegrationException {
        DataPaymentRequest dataPaymentRequest = new DataPaymentRequest();
        dataPaymentRequest.setClientReference(transactionId);
        dataPaymentRequest.setPin(generateEncryptedPin().orElseGet(() -> Strings.EMPTY));
        dataPaymentRequest.setService(paymentRequest.getBillerId());
        paymentRequest.getData().forEach(paramNameValue -> {
            if (PHONE.equals(paramNameValue.getName())){ dataPaymentRequest.setPhone(paramNameValue.getValue()); }
            if (PAYMENT_METHOD.equals(paramNameValue.getName())){ dataPaymentRequest.setPaymentMethod(paramNameValue.getValue()); }
            if ("plan".equals(paramNameValue.getName())){ dataPaymentRequest.setCode(paramNameValue.getValue()); }
        });
        return dataPaymentRequest;
    }

    private Item getChannelsAsItem(){
        if (channelsSubList.isEmpty()) {
            channelsSubList.add(new SubItem("ATM"));
            channelsSubList.add(new SubItem("B2B"));
            channelsSubList.add(new SubItem("WEB"));
            channelsSubList.add(new SubItem("MOBILE"));
            channelsSubList.add(new SubItem("ANDROIDPOS"));
            channelsSubList.add(new SubItem("LINUXPOS"));
        }
        Item itemChannel = new Item();
        itemChannel.setParamName(CHANNEL);
        itemChannel.getSubItems().addAll(channelsSubList);
        return itemChannel;
    }

    private Item getPaymentMethodAsItem(){
        if (paymentMethodSubList.isEmpty()) {
            paymentMethodSubList.add(new SubItem("card"));
            paymentMethodSubList.add(new SubItem("cash"));
        }
        Item itemPaymentMethod = new Item();
        itemPaymentMethod.setParamName(PAYMENT_METHOD);
        itemPaymentMethod.getSubItems().addAll(paymentMethodSubList);
        return itemPaymentMethod;
    }

    private PaymentItemsResponse getElectricityPaymentItems(String billerId, String categoryId){
        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item(METER_NO));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemAccountType = new Item();
        itemAccountType.setParamName(ACCOUNT_TYPE);
        itemAccountType.getSubItems().add(new SubItem("prepaid"));
        itemAccountType.getSubItems().add(new SubItem("postpaid"));
        itemAccountType.getSubItems().add(new SubItem("smartcard"));
        paymentItemsResponse.getItems().add(itemAccountType);
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getAirtimePaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item("phone"));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        paymentItemsResponse.getItems().add(getPaymentMethodAsItem());
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getDataPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getCableTvPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item(ACCOUNT));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemType = new Item("type");
        itemType.setIsAccountFixed(true);
        itemType.getSubItems().add(new SubItem("DSTV"));
        itemType.getSubItems().add(new SubItem("GOTV"));
        itemType.getSubItems().add(new SubItem("default", "DEFAULT"));
        SubItem subItemTopUp = new SubItem("topup", "TOPUP");
        subItemTopUp.setMinAmount("20");
        itemType.getSubItems().add(subItemTopUp);
        paymentItemsResponse.getItems().add(itemType);
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getInternetPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item(ACCOUNT));
        Item itemType = new Item("type");
        itemType.getSubItems().add(new SubItem("phone"));
        itemType.getSubItems().add(new SubItem("email"));
        itemType.getSubItems().add(new SubItem(ACCOUNT));
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getRemitaPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item("rrr"));
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getLCCPaymentItems(String billerId, String categoryId){
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item(ACCOUNT));
        paymentItemsResponse.getItems().add(getChannelsAsItem());
        return paymentItemsResponse;
    }

    private String getAmountInNaira(String amount){
        return Objects.isNull(amount) ?  BigDecimal.ZERO.toString() :  new BigDecimal(amount).divide(new BigDecimal(100)).toString();
    }
}

class BillerName {

    static final String ELECTRICITY = "Electricity";
    static final String AIRTIME = "Airtime";
    static final String DATA = "Data";
    static final String CABLE_TV = "CableTv";
    static final String INTERNET = "Internet";
    static final String REMITA = "Remita";
    static final String LCC = "LCC";

    private BillerName(){

    }
}

class ItexConstants{
    static final String TOKEN = "token";
    static final String SIGNATURE = "signature";
}