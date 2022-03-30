package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Stage;
import com.wayapay.thirdpartyintegrationservice.util.Status;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String CUSTOMER_PHONE = "customerMobile";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CUSTOMER_EMAIL = "customerEmail";
    private static final String PAYMENT_CODE = "paymentCode";
    private static final String CODE = "code";
    private static final String CURRENCY_CODE = "currencyCode";
    private static final String CURRENCY_SYMBOL = "currencySymbol";
    private static final String ITEM_CURRENCY_SYMBOL = "itemCurrencySymbol";
    private static final String SORTED_ORDER = "sortOrder";
    private static final String PICTURE_ID = "pictureId";
    private static final String ITEM_FEE = "itemFee";
    private static final String PAY_DIRECT_ITEM_CODE = "paydirectItemCode";
    @Value("${app.config.quickteller.query-transaction-url}")
    private String queryTransaction;

    private static List<BillerResponse> categoryAirtime = Arrays.asList(new BillerResponse("109", "Airtel Mobile Top-Up", "4"), new BillerResponse("108", "Airtel Recharge Pins", "4"), new BillerResponse("120", "Etisalat Recharge Top-Up", "4"),
            new BillerResponse("402", "Glo QuickCharge", "4"),
            new BillerResponse("109", "MTN e-Charge Prepaid", "4"),
            new BillerResponse("110", "VisaFone Data Plan", "4"),
            new BillerResponse("913", "Visafone Topup", "4"),
            new BillerResponse("910112", "Voucher Service", "4")
    );
    private static final  String INVALID_BILLER_MESSAGE = "Invalid Biller provided";
    private static final  String SELECTED_ITEM_PARAM_NAME = "Item";

    public QuickTellerService(AppConfig appConfig, QuickTellerFeignClient feignClient) {
        this.appConfig = appConfig;
        this.feignClient = feignClient;
    }

    private Map<String, String> generateHeader(HttpMethod httpMethod, String url){
        try {
            RequestHeaders requestHeaders = new RequestHeaders();
            return requestHeaders.getISWAuthSecurityHeaders(appConfig.getQuickteller().getClientId(), appConfig.getQuickteller().getSecret(), url, httpMethod.toString());
        } catch (Exception exception) {
            log.error("Exception occurred while trying to fetch the header : ", exception);
        }
        return new HashMap<>();
    }

    private String getAuthorisation(Map<String, String> headers){
        return headers.get("AUTHORIZATION");
    }

    private String getNonce(Map<String, String> headers){
        return headers.get("NONCE");
    }

    private String getTimeStamp(Map<String, String> headers){
        return headers.get("TIMESTAMP");
    }

    private String getSignature(Map<String, String> headers){
        return headers.get("SIGNATURE");
    }

    private String getSignatureMethod(Map<String, String> headers){
        return headers.get("SIGNATURE_METHOD");
    }

    //todo cache the request and response
    @Override
    public List<com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse> getCategory() throws ThirdPartyIntegrationException {

        Optional<CategoryResponse> categoryResponseOptional = Optional.empty();

        try {
            Map<String, String> headers = generateHeader(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getBillerCategoryUrl());
            categoryResponseOptional = Optional.of(feignClient.getCategory(getAuthorisation(headers), getSignature(headers), getNonce(headers), getTimeStamp(headers), getSignatureMethod(headers)));
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
            Map<String, String> headers = generateHeader(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getBillersUrl());
            billersResponseOptional = Optional.of(feignClient.getAllBillers(getAuthorisation(headers), getSignature(headers), getNonce(headers), getTimeStamp(headers), getSignatureMethod(headers)));

            log.info(" billersResponseOptional ::: " + billersResponseOptional);

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
            Map<String, String> headers = generateHeader(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getBillerPaymentItemUrl().replace("{billerId}", billerId));
            billerPaymentItemsResponseOptional = Optional.of(feignClient.getBillerPaymentItems(billerId, getAuthorisation(headers), getSignature(headers), getNonce(headers), getTimeStamp(headers), getSignatureMethod(headers)));
        } catch (FeignException e) {
            log.error("Unable to fetch billers paymentitems, billerId is => {} from interswitch ", billerId, e);
        }

        GetBillerPaymentItemResponse getBillerPaymentItemResponse = billerPaymentItemsResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch biller's payment items"));
        return getPaymentItemResponse(categoryId, billerId, getBillerPaymentItemResponse);
    }

    public QueryTransactionResponse queryTransaction(String transactionReference){
        Optional<QueryTransactionResponse> billerPaymentItemsResponseOptional = Optional.empty();
        try {
            System.out.println("url " + appConfig.getQuickteller().getBaseUrl() + queryTransaction.replace("{transactionID}", transactionReference)+"?requestreference="+transactionReference);
            Map<String, String> headers = generateHeader(HttpMethod.GET, appConfig.getQuickteller().getBaseUrl() + queryTransaction.replace("{transactionID}", transactionReference)+"?requestreference="+transactionReference);
            billerPaymentItemsResponseOptional = Optional.of(feignClient.getQueryTransaction(transactionReference, getAuthorisation(headers), getSignature(headers), getNonce(headers), getTimeStamp(headers), getSignatureMethod(headers), appConfig.getQuickteller().getTerminalId()));
            log.info("billerPaymentItemsResponseOptional :: " + billerPaymentItemsResponseOptional);
        } catch (FeignException e) {
            log.error("Unable to fetch transaction status is => {} from interswitch ", transactionReference, e);
        }
        System.out.println(billerPaymentItemsResponseOptional.get());

        return billerPaymentItemsResponseOptional.get();
    }

    @Override
    public CustomerValidationResponse validateCustomerValidationFormByBiller(CustomerValidationRequest request) throws ThirdPartyIntegrationException {
        //get the customerIds and send for validation
        Optional<QuickTellerCustomerValidationResponse> quickTellerCustomerValidationResponseOptional = Optional.empty();
        try {
            BillerDetail billerDetail = billerDetailMap.get(request.getBillerId());
//            if (Objects.isNull(billerDetail)){
//                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, INVALID_BILLER_MESSAGE);
//            }


            QuickTellerCustomerValidationRequest validationRequest = generateValidationRequest(request, new QuickTellerCustomerValidationRequest(), billerDetail);

            Map<String, String> headers = generateHeader(HttpMethod.POST, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getCustomerValidationUrl());
            quickTellerCustomerValidationResponseOptional = Optional.of(feignClient.validateCustomerInfo(validationRequest, getAuthorisation(headers), getSignature(headers), getNonce(headers), getTimeStamp(headers), getSignatureMethod(headers), appConfig.getQuickteller().getTerminalId()));
        } catch (FeignException e) {
            log.error("Unable to process customer validation against interswitch ", e);
        }

        QuickTellerCustomerValidationResponse response = quickTellerCustomerValidationResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to validate customer details"));
        return getCustomerValidationResponse(request.getCategoryId(), request.getBillerId(), response);
    }

    @Override
    @AuditPaymentOperation(stage = Stage.CONTACT_VENDOR_TO_PROVIDE_VALUE, status = Status.IN_PROGRESS)
    public PaymentResponse processPayment(PaymentRequest request, BigDecimal fee, String transactionId, String username) throws ThirdPartyIntegrationException {
        log.info("The Request  ::: " + request);
        Optional<SendPaymentAdviceResponse> sendPaymentAdviceResponseOptional = Optional.empty();
        try {

          //  BillerDetail billerDetail1 = billerDetailMap.get(request.getBillerId());
            BillerDetail billerDetail = new BillerDetail();

            if (Objects.isNull(billerDetail.getCustomerId())){
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, INVALID_BILLER_MESSAGE);
            }

            Map<String, String> headers = generateHeader(HttpMethod.POST, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getSendPaymentAdviceUrl());
            SendPaymentAdviceRequest sendPaymentAdviceRequest = generateRequest(request, billerDetail, getTimeStamp(headers));

            sendPaymentAdviceResponseOptional = Optional.of(feignClient.sendPaymentAdvice(sendPaymentAdviceRequest, getAuthorisation(headers), getSignature(headers), getNonce(headers), getTimeStamp(headers), getSignatureMethod(headers), appConfig.getQuickteller().getTerminalId()));
        } catch (FeignException e) {
            log.error("Unable to process payment against interswitch ", e);
        }

        SendPaymentAdviceResponse paymentAdviceResponse = sendPaymentAdviceResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to process payment"));

        if (SUCCESSFUL.equals(paymentAdviceResponse.getResponseCode())){
            return getPaymentResponse(paymentAdviceResponse);
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, paymentAdviceResponse.getResponseMessage());
    }

    @Override
    @AuditPaymentOperation(stage = Stage.CONTACT_VENDOR_TO_PROVIDE_VALUE, status = Status.IN_PROGRESS)
    public PaymentResponse processMultiplePayment(MultiplePaymentRequest request, BigDecimal fee, String transactionId, String username) throws ThirdPartyIntegrationException {
        Optional<SendPaymentAdviceResponse> sendPaymentAdviceResponseOptional = Optional.empty();
        try {
            BillerDetail billerDetail = billerDetailMap.get(request.getBillerId());
            if (Objects.isNull(billerDetail)){
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, INVALID_BILLER_MESSAGE);
            }

            Map<String, String> headers = generateHeader(HttpMethod.POST, appConfig.getQuickteller().getBaseUrl() + appConfig.getQuickteller().getSendPaymentAdviceUrl());
            SendPaymentAdviceRequest sendPaymentAdviceRequest = generateRequestMultiple(request, billerDetail, getTimeStamp(headers));
            sendPaymentAdviceResponseOptional = Optional.of(feignClient.sendPaymentAdvice(sendPaymentAdviceRequest, getAuthorisation(headers), getSignature(headers), getNonce(headers), getTimeStamp(headers), getSignatureMethod(headers), appConfig.getQuickteller().getTerminalId()));
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
        String paymentCode = Strings.EMPTY;
        String customerId = Strings.EMPTY;
        String customerEmail = Strings.EMPTY;
        String customerMobile = Strings.EMPTY;
      //  String paydirectItemCode = Strings.EMPTY;
        String code = Strings.EMPTY;
        for (ParamNameValue paramNameValue : paymentRequest.getData()) {
            if (paramNameValue.getName().equalsIgnoreCase(PAYMENT_CODE)){
                log.info("PAYMENT_CODE ::: " + paramNameValue.getValue());
                paymentCode = paramNameValue.getValue();
            }
            if (paramNameValue.getName().equalsIgnoreCase(CUSTOMER_ID)){
                log.info("getCustomerId ::: " + paramNameValue.getValue());
                customerId = paramNameValue.getValue();
            }
            if (paramNameValue.getName().equalsIgnoreCase(CUSTOMER_EMAIL)){
                log.info("CUSTOMER_EMAIL ::: " + paramNameValue.getValue());
                customerEmail = paramNameValue.getValue();
            }
            if (paramNameValue.getName().equalsIgnoreCase(CUSTOMER_PHONE)){ 
                if (paramNameValue.getValue().startsWith("+")) {
                    customerMobile = paramNameValue.getValue().substring(1);
                    log.info("substring ::: " + customerMobile);
                }else{
                    customerMobile = paramNameValue.getValue(); 
                }
 
            }
//            if (paramNameValue.getName().equalsIgnoreCase(PAY_DIRECT_ITEM_CODE)){
//                log.info("PAY_DIRECT_ITEM_CODE ::: " + paramNameValue.getValue());
//                paydirectItemCode = paramNameValue.getValue();
//            }
            if (paramNameValue.getName().equalsIgnoreCase(CODE)){
                log.info("CODE ::: " + paramNameValue.getValue());
                code = paramNameValue.getValue();
            }

        }
        SendPaymentAdviceRequest request = new SendPaymentAdviceRequest();
        request.setAmount(getAmountInKobo(String.valueOf(paymentRequest.getAmount())));
        request.setCustomerId(customerId);
        request.setCustomerMobile(customerMobile);
        request.setPaymentCode(paymentCode);
        request.setCustomerEmail(customerEmail);
        request.setTerminalId(appConfig.getQuickteller().getTerminalId());
        request.setRequestReference(appConfig.getQuickteller().getTransactionRefCode()+timeStamp);
        return request;
    }

    private String generateCustomerID(){
        Random random = new Random();
        String refCode = String.format("%011d", random.nextInt(10000));
        log.info("refCode :: " + refCode);
       return refCode;
    }

    private SendPaymentAdviceRequest generateRequestMultiple(MultiplePaymentRequest paymentRequest, BillerDetail billerDetail, String timeStamp){
        QuickTellerUserParam userParam = getUserParam(paymentRequest.getData(), billerDetail);
        SendPaymentAdviceRequest request = new SendPaymentAdviceRequest();
        request.setAmount(getAmountInKobo(String.valueOf(paymentRequest.getAmount())));
        request.setCustomerId(userParam.getCustomerId());
        request.setCustomerMobile(userParam.getCustomerMobile());
        request.setPaymentCode(userParam.getPaymentCode());
        request.setTerminalId(appConfig.getQuickteller().getTerminalId());
        request.setRequestReference(appConfig.getQuickteller().getTransactionRefCode()+timeStamp);
        return request;
    }

    private QuickTellerCustomerValidationRequest generateValidationRequest(CustomerValidationRequest request,
                                                                           QuickTellerCustomerValidationRequest validationRequest,
                                                                           BillerDetail billerDetail){


        QuickTellerUserParam userParam = getUserParam(request.getData(), billerDetail);
        System.out.println(" userParam ::: " + userParam);
        userParam.setCustomerId(request.getData().get(0).getValue());
        userParam.setPaymentCode(userParam.getPaymentCode());

        //paymentCode request.getData().get(1).getValue()
        validationRequest.getCustomers().add(new ValidationRequest(userParam.getCustomerId(), userParam.getPaymentCode()));
//        validationRequest.getCustomers().add(new ValidationRequest(customerId2, paymentCode));
        return validationRequest;
    }

    private QuickTellerUserParam getUserParam(List<ParamNameValue> data, BillerDetail billerDetail){
        String customerId1 = Strings.EMPTY;
        String customerId2 = Strings.EMPTY;
        String paymentCode = Strings.EMPTY;
        String customerId = Strings.EMPTY;
        String customerEmail = Strings.EMPTY;
        String customerMobile = Strings.EMPTY;

        BillerDetail billerDetail2 = new BillerDetail();
        log.info("Data => {}", data);
        log.info("BillerDetail => {}", billerDetail);

        for (ParamNameValue paramNameValue : data) {

            if (Objects.isNull(paramNameValue.getName())){
                continue;
            }

            if (paramNameValue.getName().equalsIgnoreCase(PAYMENT_CODE)){
                log.info("PAYMENT_CODE ::: " + paramNameValue.getValue());
                paymentCode = paramNameValue.getValue();
            }
            if (paramNameValue.getName().equalsIgnoreCase(billerDetail2.getCustomerId())){
                log.info("getCustomerId ::: " + paramNameValue.getValue());
                customerId = paramNameValue.getValue();
            }
            if (paramNameValue.getName().equalsIgnoreCase(CUSTOMER_EMAIL)){
                log.info("CUSTOMER_EMAIL ::: " + paramNameValue.getValue());
                customerEmail = paramNameValue.getValue();
            }
            if (paramNameValue.getName().equalsIgnoreCase(CUSTOMER_PHONE)){
                log.info("CUSTOMER_PHONE ::: " + paramNameValue.getValue());
                customerMobile = paramNameValue.getValue();
            }

//            if (paramNameValue.getName().equals(billerDetail.getCustomerfield1())){
//                customerId1 = paramNameValue.getValue();
//            }
//
//            if (paramNameValue.getName().equals(billerDetail.getCustomerfield2())){
//                customerId2 = paramNameValue.getValue();
//            }
//
//            if (paramNameValue.getName().equals(SELECTED_ITEM_PARAM_NAME)){
//                paymentCode = paramNameValue.getValue();
//            }

        }
//(paymentCode,customerId,customerEmail,customerMobile)
        return new QuickTellerUserParam(paymentCode,customerId,customerEmail,customerMobile);
    }

    private CustomerValidationResponse getCustomerValidationResponse(String categoryId, String billerId, QuickTellerCustomerValidationResponse validationResponse) throws ThirdPartyIntegrationException {
        CustomerValidationResponse customerValidationResponse = new CustomerValidationResponse(categoryId, billerId);
        log.info("validation response is {}", validationResponse);
        ValidationResponse validationResponse1 = validationResponse.getCustomers().stream().findFirst().orElse(new ValidationResponse());
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
        return new BigDecimal(amount).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.UNNECESSARY).toString();
    }

    private PaymentItemsResponse getPaymentItemResponse(String categoryId, String billerId, GetBillerPaymentItemResponse getBillerPaymentItemResponse) throws ThirdPartyIntegrationException {
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);


        log.info("HErerere :: " + getBillerPaymentItemResponse);
        BillerDetail billerDetail = new BillerDetail();
                //billerDetailMap.get(billerId);
//       List<PaymentItem> paymentitems =  getBillerPaymentItemResponse.getPaymentitems();
//        for (int i = 0; i < paymentitems.size(); i++) {
//            paymentItemsResponse.getItems().add(new Item(paymentitems.get(i).getPaymentitemname()));
//        }
//        if (Objects.isNull(billerDetail)){
//            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, INVALID_BILLER_MESSAGE);
//        }

        paymentItemsResponse.getItems().add(new Item(billerDetail.getCustomerEmail()));
        paymentItemsResponse.getItems().add(new Item(billerDetail.getCustomerId()));
        paymentItemsResponse.getItems().add(new Item(billerDetail.getCustomerMobile()));
        paymentItemsResponse.getItems().add(new Item(billerDetail.getPaymentCode()));
        paymentItemsResponse.setIsValidationRequired(true);

        Item item = new Item(SELECTED_ITEM_PARAM_NAME);

        getBillerPaymentItemResponse.getPaymentitems().forEach(paymentItem -> {
            if (paymentItem.getIsAmountFixed()) {
                item.setIsAmountFixed(paymentItem.getIsAmountFixed());
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
    static final String SUCCESSFUL = "90000";
}
