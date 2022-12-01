package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.TransactionLog;
import com.wayapay.thirdpartyintegrationservice.repo.TransactionLogRepository;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.Stage;
import com.wayapay.thirdpartyintegrationservice.util.Status;
import feign.FeignException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.wayapay.thirdpartyintegrationservice.service.baxi.BillerCategoryName.*;

@Slf4j
@Service
public class BaxiService implements IThirdPartyService {

    private final AppConfig appConfig;
    private final BaxiFeignClient feignClient;
    private static final String SUCCESS_RESPONSE_CODE = "200";
    private static final String AMOUNT = "amount";
    private static final String PHONE = "phone";
    private static final String ACCOUNT_NUMBER = "account_number";
    private static final String WALLET_FUNDING = "WALLET_FUNDING";

    @Autowired
    TransactionLogRepository transactionLogRepository;

    public BaxiService(AppConfig appConfig, BaxiFeignClient feignClient) {
        this.appConfig = appConfig;
        this.feignClient = feignClient;
    }

    @Override
    public List<com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse> getCategory() throws ThirdPartyIntegrationException {

        Optional<CategoryResponse> categoryResponseOptional = Optional.empty();
        try {
            categoryResponseOptional = Optional.of(feignClient.getCategory(appConfig.getBaxi().getXApiKey()));
        } catch (FeignException e) {
            log.error("Unable to get categories from Baxi ", e);
        }

        if (!categoryResponseOptional.isPresent()){
            log.error("No response from Baxi");
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch categories");
        }

        CategoryResponse categoryResponse = categoryResponseOptional.get();
        if (categoryResponse.getCode().equals(SUCCESS_RESPONSE_CODE)) {
            log.info("responding with list of categories");
            return categoryResponse.getData().parallelStream().map(category -> new com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse(category.getService_type(), category.getName())).collect(Collectors.toList());
        }

        log.error("response from BAxi while trying to get categories is {}", categoryResponse);
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, categoryResponse.getMessage());
    }

    @Override
    public List<BillerResponse> getAllBillersByCategory(String categoryId) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(categoryId)){
            log.error("categoryId provided id null or empty");
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid categoryId");
        }

        Optional<GetAllBillersByCategoryResponse> allBillersByCategoryOptional = Optional.empty();
        try {
            if (categoryId.equalsIgnoreCase(ELECTRICITY)){
                allBillersByCategoryOptional = Optional.of(feignClient.getAllBillersByElectricityCategory(appConfig.getBaxi().getXApiKey(), categoryId));
            } else {
                allBillersByCategoryOptional = Optional.of(feignClient.getAllBillersByCategory(appConfig.getBaxi().getXApiKey(), categoryId));
            }
        } catch (FeignException e) {
            log.error("Unable to fetch all billers by category => {}", categoryId, e);
        }

        GetAllBillersByCategoryResponse getAllBillersByCategoryResponse = allBillersByCategoryOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch billers by category"));
        if(getAllBillersByCategoryResponse.getCode().equals(SUCCESS_RESPONSE_CODE)){
            return getAllBillersByCategoryResponse.getData().getProviders().parallelStream().map(billerDetail -> new BillerResponse(billerDetail.getService_type(), billerDetail.getName(), categoryId)).collect(Collectors.toList());
        }

        log.error("response from Baxi while trying to get billers by category is {}", getAllBillersByCategoryResponse);
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getAllBillersByCategoryResponse.getMessage());
    }

    @Override
    public PaymentItemsResponse getCustomerValidationFormByBiller(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(categoryId) || CommonUtils.isEmpty(billerId)){
            log.error("categoryId => {} or billerId => {} is empty/null ", categoryId, billerId);
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category or biller provided");
        }

        switch (categoryId){
            case AIRTIME:
                return getAirtimePaymentItems(categoryId, billerId);
            case DATABUNDLE:
                return getDataBundlePaymentItems(categoryId, billerId);
            case SPECTRANET:
                return getSpectranetBundlePaymentItems(categoryId, billerId);
            case CABLETV:
                return getCableTvPaymentItems(categoryId, billerId);
            case ELECTRICITY:
                return getElectricityPaymentItems(categoryId, billerId);
            case EPIN:
                return getEPinBundlePaymentItems(categoryId, billerId);
            default:
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid CategoryId provided");
        }
    }

    @Override
    public CustomerValidationResponse validateCustomerValidationFormByBiller(CustomerValidationRequest request) throws ThirdPartyIntegrationException {
        System.out.println("START :: " + request);

        if (CommonUtils.isEmpty(request.getCategoryId()) || CommonUtils.isEmpty(request.getBillerId())){
            log.error("categoryId => {} or billerId => {} is empty/null ", request.getCategoryId(), request.getBillerId());
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category or biller provided");
        }


        switch (request.getCategoryId()){
            case AIRTIME:
            case DATABUNDLE:
            case EPIN:
                return new CustomerValidationResponse(request.getCategoryId(), request.getBillerId());
            case SPECTRANET:
                return validateSpectranet(request);
            case BETTING:
                return validateBetting(request);
            case CABLETV:
                return validateCableTv(request);
            case ELECTRICITY:
                return validateElectricity(request);
            default:
                if(request.getBillerId() !=null){
                    return validateSpectranet(request);
                }
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid CategoryId provided");
        }
    }

    @Override
    @AuditPaymentOperation(stage = Stage.CONTACT_VENDOR_TO_PROVIDE_VALUE, status = Status.IN_PROGRESS)
    public PaymentResponse processPayment(PaymentRequest request, BigDecimal fee, String transactionId, String username) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(request.getCategoryId()) || CommonUtils.isEmpty(request.getBillerId())){
            log.error("categoryId => {} or billerId => {} is empty/null ", request.getCategoryId(), request.getBillerId());
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category or biller provided");
        }

        switch (request.getCategoryId()){
            case AIRTIME:
                return airtimePayment(request, transactionId);
            case DATABUNDLE:
                return bundlePayment(request, transactionId);
            case EPIN:
                return epinPayment(request, transactionId);
            case CABLETV:
                return cableTvPayment(request, transactionId);
            case ELECTRICITY:
                return electricityPayment(request, transactionId);
            case BETTING:
                return bettingPayment(request, transactionId);
            case INSURANCE:
                return insurancePayment(request, transactionId);
            default:
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid CategoryId provided");
        }

    }

    public Object reQueryTransaction(String agentReference){
        Optional<Object> reQueryTransaction = Optional.empty();
        try {
            reQueryTransaction = Optional.of(feignClient.reQueryTransaction(appConfig.getBaxi().getXApiKey(), agentReference));
            System.out.println("electricPaymentResponseOptional ;;;;" + reQueryTransaction);
        } catch (FeignException e) {
            log.error("Unable to process customer epin payment via baxi ", e);
        }
        return reQueryTransaction;
    }

    @Override
    @AuditPaymentOperation(stage = Stage.CONTACT_VENDOR_TO_PROVIDE_VALUE, status = Status.IN_PROGRESS)
    public PaymentResponse processMultiplePayment(MultiplePaymentRequest request, BigDecimal fee, String transactionId, String username) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(request.getCategoryId()) || CommonUtils.isEmpty(request.getBillerId())){
            log.error("categoryId => {} or billerId => {} is empty/null ", request.getCategoryId(), request.getBillerId());
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category or biller provided");
        }

        switch (request.getCategoryId()){
            case AIRTIME:
                return airtimePaymentMultiple(request, transactionId);
            case DATABUNDLE:
                return bundlePaymentMultiple(request, transactionId);
            case EPIN:
                return epinPaymentMultiple(request, transactionId);
            case CABLETV:
                return cableTvPaymentMultiple(request, transactionId);
            case ELECTRICITY:
                return electricityPaymentMultiple(request, transactionId);
            default:
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid CategoryId provided");
        }

    }

    private PaymentResponse electricityPayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);
        ElectricPaymentRequest electricPaymentRequest = new ElectricPaymentRequest();
        electricPaymentRequest.setAgentId(appConfig.getBaxi().getAgentCode());
        electricPaymentRequest.setAgentReference(transactionId);
        electricPaymentRequest.setAmount(String.valueOf(request.getAmount()));
        electricPaymentRequest.setService_type(request.getBillerId());
        electricPaymentRequest.setMetadata("");
        request.getData().forEach(paramNameValue -> {
            if (ACCOUNT_NUMBER.equals(paramNameValue.getName())){ electricPaymentRequest.setAccount_number(paramNameValue.getValue()); }
            if (PHONE.equals(paramNameValue.getName())){ electricPaymentRequest.setPhone(paramNameValue.getValue()); }
        });
        logTransactionElectricity(electricPaymentRequest, String.valueOf(request.getAmount()), tranID);
        Optional<ElectricPaymentResponse> electricPaymentResponseOptional = Optional.empty();
        try {
            electricPaymentResponseOptional = Optional.of(feignClient.electricityPayment(appConfig.getBaxi().getXApiKey(), electricPaymentRequest));
        } catch (FeignException e) {
            log.error("Unable to process customer epin payment via baxi ", e);
        }

        ElectricPaymentResponse electricPaymentResponse = electricPaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        responseLogElectricity(electricPaymentResponse.getData(),tranID);
        if(SUCCESS_RESPONSE_CODE.equals(electricPaymentResponse.getCode()) && !Objects.isNull(electricPaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", electricPaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", electricPaymentResponse.getData().getTransactionMessage()));
            paymentResponse.getData().add(new ParamNameValue("tokenCode", electricPaymentResponse.getData().getTokenCode()));
            paymentResponse.getData().add(new ParamNameValue("tokenAmount", electricPaymentResponse.getData().getTokenAmount()));
            paymentResponse.getData().add(new ParamNameValue("amountOfPower", electricPaymentResponse.getData().getAmountOfPower()));
            paymentResponse.getData().add(new ParamNameValue("creditToken", electricPaymentResponse.getData().getCreditToken()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse electricityPaymentMultiple(MultiplePaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);
        ElectricPaymentRequest electricPaymentRequest = new ElectricPaymentRequest();
        electricPaymentRequest.setAgentId(appConfig.getBaxi().getAgentCode());
        electricPaymentRequest.setAgentReference(transactionId);
        electricPaymentRequest.setAmount(String.valueOf(request.getAmount()));
        electricPaymentRequest.setService_type(request.getBillerId());
        request.getData().forEach(paramNameValue -> {
            if (ACCOUNT_NUMBER.equals(paramNameValue.getName())){ electricPaymentRequest.setAccount_number(paramNameValue.getValue()); }
            if (PHONE.equals(paramNameValue.getName())){ electricPaymentRequest.setPhone(paramNameValue.getValue()); }
        });
        logTransactionElectricity(electricPaymentRequest, String.valueOf(request.getAmount()), tranID);
        Optional<ElectricPaymentResponse> electricPaymentResponseOptional = Optional.empty();
        try {
            electricPaymentResponseOptional = Optional.of(feignClient.electricityPayment(appConfig.getBaxi().getXApiKey(), electricPaymentRequest));
        } catch (FeignException e) {
            log.error("Unable to process customer epin payment via baxi ", e);
        }

        ElectricPaymentResponse electricPaymentResponse = electricPaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        responseLogElectricity(electricPaymentResponse.getData(),tranID);
        if(SUCCESS_RESPONSE_CODE.equals(electricPaymentResponse.getCode()) && !Objects.isNull(electricPaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", electricPaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", electricPaymentResponse.getData().getTransactionMessage()));
            paymentResponse.getData().add(new ParamNameValue("tokenCode", electricPaymentResponse.getData().getTokenCode()));
            paymentResponse.getData().add(new ParamNameValue("tokenAmount", electricPaymentResponse.getData().getTokenAmount()));
            paymentResponse.getData().add(new ParamNameValue("amountOfPower", electricPaymentResponse.getData().getAmountOfPower()));
            paymentResponse.getData().add(new ParamNameValue("creditToken", electricPaymentResponse.getData().getCreditToken()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse cableTvPayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);
        CablePaymentRequest cablePaymentRequest = new CablePaymentRequest();
        request.getData().forEach(paramNameValue -> {
            if ("plan".equals(paramNameValue.getName())){ cablePaymentRequest.setProductCode(paramNameValue.getValue()); }
            if ("smartcard_number".equals(paramNameValue.getName())){ cablePaymentRequest.setSmartcardNumber(paramNameValue.getValue()); }
            if ("phone".equals(paramNameValue.getName())){ cablePaymentRequest.setPhone(paramNameValue.getValue()); }
            if ("product_monthsPaidFor".equals(paramNameValue.getName())){ cablePaymentRequest.setProductMonthsPaidFor(paramNameValue.getValue()); }

        });

        cablePaymentRequest.setAgentId(appConfig.getBaxi().getAgentCode());
        cablePaymentRequest.setAgentReference(tranID);
        cablePaymentRequest.setServiceType(request.getBillerId());
        cablePaymentRequest.setTotalAmount(String.valueOf(request.getAmount()));

        logTransactionCableTv(cablePaymentRequest, String.valueOf(request.getAmount()), tranID);

      //  String cableTvAddons = getCableTvAddons(request);

        Optional<CablePaymentResponse> cablePaymentResponseOptional = Optional.empty();
        String errorMessage = null;
        try {
            //cablePaymentRequest.getSmartcardNumber(),, cablePaymentRequest.getProductCode(), "1", cableTvAddons, "0", request.getBillerId(), appConfig.getBaxi().getAgentCode(), tranID
            cablePaymentResponseOptional = Optional.of(feignClient.cableTvPayment(appConfig.getBaxi().getXApiKey(), cablePaymentRequest));
        } catch (FeignException e) {
            logErrorResponse(e,tranID);
            log.error("Unable to process customer cableTv payment via baxi ", e);
            errorMessage = getErrorMessage(e.contentUTF8());
        }

        String finalErrorMessage = errorMessage;
        CablePaymentResponse cablePaymentResponse = cablePaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Objects.isNull(finalErrorMessage) ? Constants.ERROR_MESSAGE : finalErrorMessage));
        responseLogTransaction(cablePaymentResponse.getData(), tranID);
        if(SUCCESS_RESPONSE_CODE.equals(cablePaymentResponse.getCode()) && !Objects.isNull(cablePaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", cablePaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", cablePaymentResponse.getData().getTransactionMessage()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse cableTvPaymentMultiple(MultiplePaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);
        CablePaymentRequest cablePaymentRequest = new CablePaymentRequest();
        request.getData().forEach(paramNameValue -> {
            if ("plan".equals(paramNameValue.getName())){ cablePaymentRequest.setProductCode(paramNameValue.getValue()); }
            if ("smartcard_number".equals(paramNameValue.getName())){ cablePaymentRequest.setSmartcardNumber(paramNameValue.getValue()); }
            if ("phone".equals(paramNameValue.getName())){ cablePaymentRequest.setPhone(paramNameValue.getValue()); }
            if ("product_monthsPaidFor".equals(paramNameValue.getName())){ cablePaymentRequest.setProductMonthsPaidFor(paramNameValue.getValue()); }

        });

        cablePaymentRequest.setAgentId(appConfig.getBaxi().getAgentCode());
        cablePaymentRequest.setAgentReference(tranID);
        cablePaymentRequest.setServiceType(request.getBillerId());
        cablePaymentRequest.setTotalAmount(String.valueOf(request.getAmount()));
        logTransactionCableTv(cablePaymentRequest, String.valueOf(request.getAmount()), tranID);

        //String cableTvAddons = getCableTvAddonsMultiple(request);

        Optional<CablePaymentResponse> cablePaymentResponseOptional = Optional.empty();
        String errorMessage = null;
        try {
            cablePaymentResponseOptional = Optional.of(feignClient.cableTvPayment(appConfig.getBaxi().getXApiKey(), cablePaymentRequest));
        } catch (FeignException e) {
            log.error("Unable to process customer cableTv payment via baxi ", e);
            errorMessage = getErrorMessage(e.contentUTF8());
        }

        String finalErrorMessage = errorMessage;
        CablePaymentResponse cablePaymentResponse = cablePaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Objects.isNull(finalErrorMessage) ? Constants.ERROR_MESSAGE : finalErrorMessage));
        responseLogTransaction(cablePaymentResponse.getData(), tranID);
        if(SUCCESS_RESPONSE_CODE.equals(cablePaymentResponse.getCode()) && !Objects.isNull(cablePaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", cablePaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", cablePaymentResponse.getData().getTransactionMessage()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private String getCableTvAddons(PaymentRequest request) throws ThirdPartyIntegrationException {
        ParamNameValue paramNameValuePlan = request.getData().stream().filter(paramNameValue -> paramNameValue.getName().equals("plan")).findFirst().orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "plan is required"));
        Optional<CableTvAddonsResponse> cableTvAddonsResponseOptional = Optional.empty();
        try {
            AddonRequest request1 = new AddonRequest();
            request1.setProductCode(paramNameValuePlan.getValue());
            request1.setServiceType(request.getBillerId());

            cableTvAddonsResponseOptional = Optional.of(feignClient.getCableTvAddons(appConfig.getBaxi().getXApiKey(), request1));
        } catch (FeignException e) {
            log.error("Unable to get addons for the cableTv smartnumber -> {}", paramNameValuePlan.getValue(), e);
        }
        CableTvAddonsResponse cableTvAddonsResponse = cableTvAddonsResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.ERROR_MESSAGE));
        return cableTvAddonsResponse.getData().stream().findFirst().orElse(new CableTvDetail()).getCode();
    }

    private String getCableTvAddonsMultiple(MultiplePaymentRequest request) throws ThirdPartyIntegrationException {
        ParamNameValue paramNameValuePlan = request.getData().stream().filter(paramNameValue -> paramNameValue.getName().equals("plan")).findFirst().orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "plan is required"));
        Optional<CableTvAddonsResponse> cableTvAddonsResponseOptional = Optional.empty();
        try {
            AddonRequest request1 = new AddonRequest();
            request1.setProductCode(paramNameValuePlan.getValue());
            request1.setServiceType(request.getBillerId());
            cableTvAddonsResponseOptional = Optional.of(feignClient.getCableTvAddons(appConfig.getBaxi().getXApiKey(), request1));
        } catch (FeignException e) {
            log.error("Unable to get addons for the cableTv smartnumber -> {}", paramNameValuePlan.getValue(), e);
        }
        CableTvAddonsResponse cableTvAddonsResponse = cableTvAddonsResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.ERROR_MESSAGE));
        return cableTvAddonsResponse.getData().stream().findFirst().orElse(new CableTvDetail()).getCode();
    }


    private PaymentResponse epinPayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);
        EPinPaymentRequest ePinPaymentRequest = new EPinPaymentRequest();
        request.getData().forEach(paramNameValue -> {
            if ("numberOfPins".equals(paramNameValue.getName())){ ePinPaymentRequest.setNumberOfPins(paramNameValue.getValue()); }
            if ("pinValue".equals(paramNameValue.getName())){ ePinPaymentRequest.setPinValue(paramNameValue.getValue()); }
        });

        logTransactionEpin(ePinPaymentRequest, String.valueOf(request.getAmount()), tranID);

        Optional<EPinPaymentResponse> ePinPaymentResponseOptional = Optional.empty();
        try {

            ePinPaymentRequest.setAgentId(appConfig.getBaxi().getAgentCode());
            ePinPaymentRequest.setAgentReference(tranID);
            ePinPaymentRequest.setAmount(String.valueOf(request.getAmount()));
            ePinPaymentRequest.setServiceType(request.getBillerId());
            ePinPaymentResponseOptional = Optional.of(feignClient.epinPayment(appConfig.getBaxi().getXApiKey(),ePinPaymentRequest));
        } catch (FeignException e) {
            log.error("Unable to process customer epin payment via baxi ", e);
        }

        EPinPaymentResponse ePinPaymentResponse = ePinPaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        responseLogTransactionEPins(ePinPaymentResponse.getData(), tranID);
        if(SUCCESS_RESPONSE_CODE.equals(ePinPaymentResponse.getCode()) && !Objects.isNull(ePinPaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", ePinPaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", ePinPaymentResponse.getData().getTransactionMessage()));
            ePinPaymentResponse.getData().getPins().forEach(pinDetail -> {
                paymentResponse.getData().add(new ParamNameValue("instructions", pinDetail.getInstructions()));
                paymentResponse.getData().add(new ParamNameValue("serialNumber", pinDetail.getSerialNumber()));
                paymentResponse.getData().add(new ParamNameValue("pin", pinDetail.getPin()));
                paymentResponse.getData().add(new ParamNameValue("expiresOn", pinDetail.getExpiresOn()));
            });
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse epinPaymentMultiple(MultiplePaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);
        EPinPaymentRequest ePinPaymentRequest = new EPinPaymentRequest();
        request.getData().forEach(paramNameValue -> {
            if ("numberOfPins".equals(paramNameValue.getName())){ ePinPaymentRequest.setNumberOfPins(paramNameValue.getValue()); }
            if ("pinValue".equals(paramNameValue.getName())){ ePinPaymentRequest.setPinValue(paramNameValue.getValue()); }
        });

        logTransactionEpin(ePinPaymentRequest,String.valueOf(request.getAmount()),tranID);

        Optional<EPinPaymentResponse> ePinPaymentResponseOptional = Optional.empty();
        try {
            ePinPaymentRequest.setAgentId(appConfig.getBaxi().getAgentCode());
            ePinPaymentRequest.setAgentReference(tranID);
            ePinPaymentRequest.setAmount(String.valueOf(request.getAmount()));
            ePinPaymentRequest.setServiceType(request.getBillerId());
            ePinPaymentResponseOptional = Optional.of(feignClient.epinPayment(appConfig.getBaxi().getXApiKey(), ePinPaymentRequest));
        } catch (FeignException e) {
            logErrorResponse(e,tranID);
            log.error("Unable to process customer epin payment via baxi ", e);
        }

        EPinPaymentResponse ePinPaymentResponse = ePinPaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        responseLogTransactionEPins(ePinPaymentResponse.getData(),tranID);
        if(SUCCESS_RESPONSE_CODE.equals(ePinPaymentResponse.getCode()) && !Objects.isNull(ePinPaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", ePinPaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", ePinPaymentResponse.getData().getTransactionMessage()));
            ePinPaymentResponse.getData().getPins().forEach(pinDetail -> {
                paymentResponse.getData().add(new ParamNameValue("instructions", pinDetail.getInstructions()));
                paymentResponse.getData().add(new ParamNameValue("serialNumber", pinDetail.getSerialNumber()));
                paymentResponse.getData().add(new ParamNameValue("pin", pinDetail.getPin()));
                paymentResponse.getData().add(new ParamNameValue("expiresOn", pinDetail.getExpiresOn()));
            });
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse bundlePayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);
        BundlePaymentRequest bundlePaymentRequest = new BundlePaymentRequest();
        request.getData().forEach(paramNameValue -> {
            if (PHONE.equals(paramNameValue.getName())){

                String phone = paramNameValue.getValue();
                System.out.println("PHONE phone" + phone);
                if (phone.startsWith("+")){
                    StringBuilder sb = new StringBuilder(phone);
                    sb.replace(0,4,"0");
                    System.out.println("PHONE = = sb" + sb);
                    bundlePaymentRequest.setPhone(sb.toString());
                }

            }
            if ("bundles".equals(paramNameValue.getName())){ bundlePaymentRequest.setDatacode(paramNameValue.getValue()); }
        });

        logTransactionData(bundlePaymentRequest,String.valueOf(request.getAmount()),request.getBillerId(),tranID);
        Optional<BundlePaymentResponse> bundlePaymentResponseOptional = Optional.empty();
        try {
            DataPayment dataPayment = getDataPayment(request, tranID, bundlePaymentRequest);
            System.out.println( "DataPayment ::: " + dataPayment);
            bundlePaymentResponseOptional = Optional.of(feignClient.bundlePayment(appConfig.getBaxi().getXApiKey(),dataPayment));
        } catch (FeignException e) {
            logErrorResponse(e,tranID);
            log.error("Unable to process customer bundle payment via baxi ", e);
        }


        BundlePaymentResponse bundlePaymentResponse = bundlePaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));

        responseLogTransaction(bundlePaymentResponse.getData(),tranID);

        if(SUCCESS_RESPONSE_CODE.equals(bundlePaymentResponse.getCode()) && !Objects.isNull(bundlePaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", bundlePaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", bundlePaymentResponse.getData().getTransactionMessage()));
            return paymentResponse;

        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private Optional<BettingPaymentRespose> bundlePaymentResponseOptional(){
        return Optional.empty();
    }

    private PaymentResponse insurancePayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);

        System.out.println("Request :: " + request.getData());

        InsurancePaymentRequest request1 = new InsurancePaymentRequest();

        ClientRequest clientRequest = new ClientRequest();

        VehicleOrderRequest vehicleOrderRequest = new VehicleOrderRequest();
        VehicleRequest vehicleRequest = new VehicleRequest();

        request.getData().forEach(paramNameValue -> {
            if ("firstName".equals(paramNameValue.getName())){  clientRequest.setFirstName(paramNameValue.getValue()); }
            if ("lastName".equals(paramNameValue.getName())){  clientRequest.setLastName(paramNameValue.getValue()); }
            if ("email".equals(paramNameValue.getName())){  clientRequest.setEmail(paramNameValue.getValue()); }
            if (PHONE.equals(paramNameValue.getName())){  clientRequest.setPhone(paramNameValue.getValue()); }
            if ("address".equals(paramNameValue.getName())){  clientRequest.setAddress(paramNameValue.getValue()); }
            if ("gender".equals(paramNameValue.getName())){  clientRequest.setGender(paramNameValue.getValue()); }
            if ("companyName".equals(paramNameValue.getName())){  clientRequest.setCompanyName(paramNameValue.getValue()); }
            if ("dateOfBirth".equals(paramNameValue.getName())){  clientRequest.setDateOfBirth(paramNameValue.getValue()); }
            if ("occupation".equals(paramNameValue.getName())){  clientRequest.setOccupation(paramNameValue.getValue()); }


            if ("vehicleType".equals(paramNameValue.getName())){  vehicleRequest.setVehicleType(paramNameValue.getValue()); }
            if ("vehicleManufacturer".equals(paramNameValue.getName())){  vehicleRequest.setVehicleManufacturer(paramNameValue.getValue()); }
            if ("vehicleModel".equals(paramNameValue.getName())){  vehicleRequest.setVehicleModel(paramNameValue.getValue()); }
            if ("registrationNumber".equals(paramNameValue.getName())){  vehicleRequest.setRegistrationNumber(paramNameValue.getValue()); }
            if ("engineNumber".equals(paramNameValue.getName())){  vehicleRequest.setEngineNumber(paramNameValue.getValue()); }
            if ("chassisNumber".equals(paramNameValue.getName())){  vehicleRequest.setChassisNumber(paramNameValue.getValue()); }
            if ("yearOfManufacture".equals(paramNameValue.getName())){  vehicleRequest.setYearOfPurchase(Long.parseLong(paramNameValue.getValue())); }
            if ("color".equals(paramNameValue.getName())){  vehicleRequest.setColor(paramNameValue.getValue()); }
            if ("yearOfPurchase".equals(paramNameValue.getName())){ vehicleRequest.setYearOfPurchase(Long.parseLong(paramNameValue.getValue())); }


            if ("vehicleTypeId".equals(paramNameValue.getName())){  vehicleOrderRequest.setVehicleTypeId(paramNameValue.getValue()); }
            if ("orderDescriptionId".equals(paramNameValue.getName())){  vehicleOrderRequest.setOrderDescriptionId(paramNameValue.getValue()); }
            if ("orderDescriptionName".equals(paramNameValue.getName())){  vehicleOrderRequest.setOrderDescriptionName(paramNameValue.getValue()); }
            if ("vehicleManufacturerId".equals(paramNameValue.getName())){  vehicleOrderRequest.setVehicleManufacturerId(paramNameValue.getValue()); }
            if ("vehicleModelId".equals(paramNameValue.getName())){  vehicleOrderRequest.setVehicleModelId(paramNameValue.getValue()); }
            if ("city".equals(paramNameValue.getName())){  vehicleOrderRequest.setCity(paramNameValue.getValue()); }
            if ("amount".equals(paramNameValue.getName())){  vehicleOrderRequest.setAmount(BigDecimal.valueOf(Double.parseDouble(paramNameValue.getValue()))); }
            if ("transactionCode".equals(paramNameValue.getName())){  vehicleOrderRequest.setTransactionCode(paramNameValue.getValue()); }
            if ("policyholder".equals(paramNameValue.getName())){  vehicleOrderRequest.setPolicyholder(paramNameValue.getValue()); }
            if ("action".equals(paramNameValue.getName())){  vehicleOrderRequest.setAction(paramNameValue.getValue()); }

        });

        request1.setClient(clientRequest);
        request1.setOrder(vehicleOrderRequest);
        request1.setVehicle(vehicleRequest);

        request1.setServiceType(request.getBillerId());
        request1.setAgentReference(appConfig.getBaxi().getAgentCode());

       // logTransactionData(bettingRequest,String.valueOf(request.getAmount()),request.getBillerId(),tranID);

        Optional<BettingPaymentRespose> bundlePaymentResponseOptional = bundlePaymentResponseOptional();
        try {

            if(request.getBillerId().equals("carpaddy")){
                bundlePaymentResponseOptional = Optional.of(feignClient.carPaddyPayment(appConfig.getBaxi().getXApiKey(),request1));
            }else{
                bundlePaymentResponseOptional = Optional.of(feignClient.insurancePayment(appConfig.getBaxi().getXApiKey(),request1));
                //carPaddyPayment
            }

        } catch (FeignException e) {
            logErrorResponse(e,tranID);
            log.error("Unable to process customer bundle payment via baxi ", e);
        }

        BettingPaymentRespose bundlePaymentResponse = bundlePaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));

        responseLogTransaction(bundlePaymentResponse.getData(),tranID);

        if(SUCCESS_RESPONSE_CODE.equals(bundlePaymentResponse.getCode()) && !Objects.isNull(bundlePaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", bundlePaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", bundlePaymentResponse.getData().getTransactionMessage()));
            return paymentResponse;

        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse bettingPayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);
        BettingRequest bettingRequest = new BettingRequest();
        request.getData().forEach(paramNameValue -> {
            if (ACCOUNT_NUMBER.equals(paramNameValue.getName())){  bettingRequest.setAccountNumber(paramNameValue.getValue()); }
        });
        bettingRequest.setAmount(String.valueOf(request.getAmount()));
        bettingRequest.setAction(WALLET_FUNDING);
        bettingRequest.setServiceType(request.getBillerId());
        bettingRequest.setAgentReference(appConfig.getBaxi().getAgentCode());
        logTransactionData(bettingRequest,String.valueOf(request.getAmount()),request.getBillerId(),tranID);

        Optional<BettingPaymentRespose> bundlePaymentResponseOptional = Optional.empty();
        try {

            bundlePaymentResponseOptional = Optional.of(feignClient.bettingPayment(appConfig.getBaxi().getXApiKey(),bettingRequest));
        } catch (FeignException e) {
            logErrorResponse(e,tranID);
            log.error("Unable to process customer bundle payment via baxi ", e);
        }


        BettingPaymentRespose bundlePaymentResponse = bundlePaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));

        responseLogTransaction(bundlePaymentResponse.getData(),tranID);

        if(SUCCESS_RESPONSE_CODE.equals(bundlePaymentResponse.getCode()) && !Objects.isNull(bundlePaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", bundlePaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", bundlePaymentResponse.getData().getTransactionMessage()));
            return paymentResponse;

        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private void logTransactionData(BettingRequest bettingRequest,String amount,String biller,String tranID){
        Map<String, Object> map = new HashMap<>();
        map.put("service_type", biller);
        map.put("account_number", bettingRequest.getAccountNumber());
        map.put("amount", amount);
        map.put("action", bettingRequest.getAction());
        map.put("transactionID", bettingRequest);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setThirdParty(BAXI);
        transactionLog.setPaymentRequest(CommonUtils.objectToJson(map).orElse(""));
        transactionLog.setRequestReference(tranID);
        transactionLogRepository.save(transactionLog);
    }




    private DataPayment getDataPayment(PaymentRequest request, String tranID, BundlePaymentRequest bundlePaymentRequest){

        DataPayment dataPayment = new DataPayment();
        dataPayment.setAgentId(appConfig.getBaxi().getAgentCode());
        dataPayment.setAgentReference(tranID);
        dataPayment.setAmount(String.valueOf(request.getAmount()));
        dataPayment.setDatacode(bundlePaymentRequest.getDatacode());
        dataPayment.setPhone(bundlePaymentRequest.getPhone());
        dataPayment.setServiceType(request.getBillerId());
        return dataPayment;
    }
    private void logTransactionEpin(EPinPaymentRequest ePinPaymentRequest, String amount, String transactionID){
        Map<String, Object> map = new HashMap<>();
        map.put("numberOfPins", ePinPaymentRequest.getNumberOfPins());
        map.put("pinValue", ePinPaymentRequest.getPinValue());
        map.put("amount", amount);
        map.put("transactionID", transactionID);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setThirdParty(BAXI);
        transactionLog.setPaymentRequest(CommonUtils.objectToJson(map).orElse(""));
        transactionLog.setRequestReference(transactionID);
        transactionLogRepository.save(transactionLog);

    }

    private void logTransactionElectricity(ElectricPaymentRequest electricPaymentRequest, String amount, String transactionID){
        Map<String, Object> map = new HashMap<>();
        map.put("serviceType", electricPaymentRequest.getService_type());
        map.put("accountNumber", electricPaymentRequest.getAccount_number());
        map.put("amount", amount);
        map.put("transactionID", transactionID);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setThirdParty(BAXI);
        transactionLog.setPaymentRequest(CommonUtils.objectToJson(map).orElse(""));
        transactionLog.setRequestReference(transactionID);
        transactionLogRepository.save(transactionLog);

    }
    private void logTransactionCableTv(CablePaymentRequest ePinPaymentRequest, String amount, String transactionID){
        Map<String, Object> map = new HashMap<>();
        map.put("productCode", ePinPaymentRequest.getProductCode());
        map.put("smartCardNumber", ePinPaymentRequest.getSmartcardNumber());
        map.put("amount", amount);
        map.put("transactionID", transactionID);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setThirdParty(BAXI);
        transactionLog.setPaymentRequest(CommonUtils.objectToJson(map).orElse(""));
        transactionLog.setRequestReference(transactionID);
        transactionLogRepository.save(transactionLog);

    }
    private void logTransactionData(BundlePaymentRequest bundlePaymentRequest, String amount, String billerId, String transactionID){
        Map<String, Object> map = new HashMap<>();
        map.put("paymentCode", bundlePaymentRequest.getDatacode());
        map.put("phone", bundlePaymentRequest.getPhone());
        map.put("amount", amount);
        map.put("billerId", billerId);
        map.put("transactionID", transactionID);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setThirdParty(BAXI);
        transactionLog.setPaymentRequest(CommonUtils.objectToJson(map).orElse(""));
        transactionLog.setRequestReference(transactionID);
        transactionLogRepository.save(transactionLog);

    }

    private void responseLogTransactionEPins(EpinDetail epinDetail, String referenceID){
        if(epinDetail !=null){
            TransactionLog transactionLog = transactionLogRepository.findByReference(referenceID);
            transactionLog.setPaymentResponse(CommonUtils.objectToJson(epinDetail).orElse(""));
            transactionLog.setStatusCode(epinDetail.getStatusCode());
            transactionLog.setStatusMessage(epinDetail.getTransactionStatus());
            transactionLogRepository.save(transactionLog);
        }

    }


    private void logErrorResponse(FeignException e, String referenceID){
        TransactionLog transactionLog = transactionLogRepository.findByReference(referenceID);
        transactionLog.setErrorResponse(CommonUtils.objectToJson(e).orElse(""));
        transactionLogRepository.save(transactionLog);
    }


    private void responseLogElectricity(ElectricDetail bundlePaymentResponse, String referenceID){
        if(bundlePaymentResponse !=null){
            TransactionLog transactionLog = transactionLogRepository.findByReference(referenceID);
            transactionLog.setPaymentResponse(CommonUtils.objectToJson(bundlePaymentResponse).orElse(""));
            transactionLog.setStatusCode(bundlePaymentResponse.getStatusCode());
            transactionLog.setStatusMessage(bundlePaymentResponse.getTransactionStatus());
            transactionLogRepository.save(transactionLog);
        }

    }

    private void responseLogTransaction(PaymentDetail bundlePaymentResponse, String referenceID){
        System.out.println("PaymentDetail ::  " + bundlePaymentResponse);
        if(bundlePaymentResponse !=null){
            TransactionLog transactionLog = transactionLogRepository.findByReference(referenceID);
            transactionLog.setPaymentResponse(CommonUtils.objectToJson(bundlePaymentResponse).orElse(""));
            transactionLog.setStatusCode(bundlePaymentResponse.getStatusCode());
            transactionLog.setStatusMessage(bundlePaymentResponse.getTransactionStatus());
            transactionLogRepository.save(transactionLog);
        }

    }


    private void logTransactionAirtime(AirtimePaymentRequest bundlePaymentRequest, String amount, String billerId, String transactionID){
        Map<String, Object> map = new HashMap<>();
        map.put("plan", bundlePaymentRequest.getPlan());
        map.put("phone", bundlePaymentRequest.getPhone());
        map.put("amount", amount);
        map.put("billerId", billerId);
        map.put("transactionID", transactionID);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setThirdParty(BAXI);
        transactionLog.setPaymentRequest(CommonUtils.objectToJson(map).orElse(""));
        transactionLog.setRequestReference(transactionID);
        transactionLogRepository.save(transactionLog);

    }

    private PaymentResponse bundlePaymentMultiple(MultiplePaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {
        String tranID = getCustomRefCode(transactionId);
        BundlePaymentRequest bundlePaymentRequest = new BundlePaymentRequest();
        request.getData().forEach(paramNameValue -> {
            if (PHONE.equals(paramNameValue.getName())){ bundlePaymentRequest.setPhone(paramNameValue.getValue()); }
            if ("bundles".equals(paramNameValue.getName())){ bundlePaymentRequest.setDatacode(paramNameValue.getValue()); }
        });

        Optional<BundlePaymentResponse> bundlePaymentResponseOptional = Optional.empty();
        try {
            DataPayment dataPayment = new DataPayment();
            dataPayment.setAgentId(appConfig.getBaxi().getAgentCode());
            dataPayment.setAgentReference(tranID);
            dataPayment.setAmount(String.valueOf(request.getAmount()));
            dataPayment.setDatacode(bundlePaymentRequest.getDatacode());
            dataPayment.setPhone(bundlePaymentRequest.getPhone());
            dataPayment.setServiceType(request.getBillerId());
            bundlePaymentResponseOptional = Optional.of(feignClient.bundlePayment(appConfig.getBaxi().getXApiKey(), dataPayment));
        } catch (FeignException e) {
            log.error("Unable to process customer bundle payment via baxi ", e);
        }

        BundlePaymentResponse bundlePaymentResponse = bundlePaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS_RESPONSE_CODE.equals(bundlePaymentResponse.getCode()) && !Objects.isNull(bundlePaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", bundlePaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", bundlePaymentResponse.getData().getTransactionMessage()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse airtimePayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {

        System.out.println("airtimePayment request :: " + request);
        String tranID = getCustomRefCode(transactionId);
        AirtimePaymentRequest airtimePaymentRequest = new AirtimePaymentRequest();
        request.getData().forEach(paramNameValue -> {
            if (PHONE.equals(paramNameValue.getName())){

            String phone = paramNameValue.getValue();
                System.out.println("PHONE phone" + phone);
                if (phone.startsWith("+")){
                    StringBuilder sb = new StringBuilder(phone);
                    sb.replace(0,4,"0");
                    System.out.println("PHONE = = sb" + sb);
                    airtimePaymentRequest.setPhone(sb.toString());
                }else{
                    airtimePaymentRequest.setPhone(phone);
                }

            }
            if ("plan".equals(paramNameValue.getName())){ airtimePaymentRequest.setPlan(paramNameValue.getValue()); }
        });



        logTransactionAirtime(airtimePaymentRequest,String.valueOf(request.getAmount()),request.getBillerId(),tranID);

        Optional<AirtimePaymentResponse> airtimePaymentResponseOptional = Optional.empty();
        try {
            AirtimePayment payment = new AirtimePayment();
            payment.setAgentId(appConfig.getBaxi().getAgentCode());
            payment.setAgentReference(tranID);
            payment.setAmount(String.valueOf(request.getAmount()));
            payment.setPhone(airtimePaymentRequest.getPhone());
            payment.setPlan(airtimePaymentRequest.getPlan());
            payment.setServiceType(request.getBillerId());
            airtimePaymentResponseOptional = Optional.of(feignClient.airtimePayment(appConfig.getBaxi().getXApiKey(), payment));   //airtimePaymentRequest.getPhone(), String.valueOf(request.getAmount()), request.getBillerId(), airtimePaymentRequest.getPlan(), appConfig.getBaxi().getAgentCode(),tranID
        } catch (FeignException e) {
            log.error("Unable to process customer airtime payment via baxi ", e);
        }

        AirtimePaymentResponse airtimePaymentResponse = airtimePaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        responseLogTransaction(airtimePaymentResponse.getData(), tranID);
        if(SUCCESS_RESPONSE_CODE.equals(airtimePaymentResponse.getCode()) && !Objects.isNull(airtimePaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", airtimePaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", airtimePaymentResponse.getData().getTransactionMessage()));
            paymentResponse.getData().add(new ParamNameValue("baxiReference", airtimePaymentResponse.getData().getBaxiReference()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private PaymentResponse airtimePaymentMultiple(MultiplePaymentRequest request, String transactionId) throws ThirdPartyIntegrationException {

        AirtimePaymentRequest airtimePaymentRequest = new AirtimePaymentRequest();
        request.getData().forEach(paramNameValue -> {
            if (PHONE.equals(paramNameValue.getName())){

                String phone = paramNameValue.getValue();
                System.out.println("PHONE phone" + phone);
                if (phone.startsWith("+")){
                    StringBuilder sb = new StringBuilder(phone);
                    sb.replace(0,4,"0");
                    System.out.println("PHONE = = sb" + sb);
                    airtimePaymentRequest.setPhone(sb.toString());
                }else{
                    airtimePaymentRequest.setPhone(phone);
                }

            }
            if ("plan".equals(paramNameValue.getName())){ airtimePaymentRequest.setPlan(paramNameValue.getValue()); }
        });

        Optional<AirtimePaymentResponse> airtimePaymentResponseOptional = Optional.empty();
        try {
            AirtimePayment payment = new AirtimePayment();
            payment.setAgentId(appConfig.getBaxi().getAgentCode());
            payment.setAgentReference(transactionId);
            payment.setAmount(String.valueOf(request.getAmount()));
            payment.setPhone(airtimePaymentRequest.getPhone());
            payment.setPlan(airtimePaymentRequest.getPlan());
            payment.setServiceType(request.getBillerId());
            airtimePaymentResponseOptional = Optional.of(feignClient.airtimePayment(appConfig.getBaxi().getXApiKey(), payment));
        } catch (FeignException e) {
            log.error("Unable to process customer airtime payment via baxi ", e);
        }

        AirtimePaymentResponse airtimePaymentResponse = airtimePaymentResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS_RESPONSE_CODE.equals(airtimePaymentResponse.getCode()) && !Objects.isNull(airtimePaymentResponse.getData())) {
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.getData().add(new ParamNameValue("transactionReference", airtimePaymentResponse.getData().getTransactionReference()));
            paymentResponse.getData().add(new ParamNameValue("transactionMessage", airtimePaymentResponse.getData().getTransactionMessage()));
            paymentResponse.getData().add(new ParamNameValue("baxiReference", airtimePaymentResponse.getData().getBaxiReference()));
            return paymentResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private CustomerValidationResponse validateCableTv(CustomerValidationRequest request) throws ThirdPartyIntegrationException {

        ParamNameValue paramNameValueAccountNumber = request.getData().stream().filter(paramNameValue -> paramNameValue.getName().equals("smartcard_number")).findFirst().orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "smart card number is required"));

        Optional<NameFinderQueryResponse> nameFinderQueryResponseOptional = Optional.empty();
        try {
            nameFinderQueryResponseOptional = Optional.of(feignClient.nameFinderEnquiry(appConfig.getBaxi().getXApiKey(), request.getBillerId(), paramNameValueAccountNumber.getValue()));
        } catch (FeignException e) {
            log.error("Unable to verify customer electricity smart card number via Baxi ", e);
        }

        NameFinderQueryResponse nameFinderQueryResponse = nameFinderQueryResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        if(SUCCESS_RESPONSE_CODE.equals(nameFinderQueryResponse.getCode()) && !Objects.isNull(nameFinderQueryResponse.getData())) {
            CustomerValidationResponse customerValidationResponse = new CustomerValidationResponse(request.getCategoryId(), request.getBillerId());
            customerValidationResponse.getData().add(new ParamNameValue("name", nameFinderQueryResponse.getData().getUser().getName()));
            customerValidationResponse.getData().add(new ParamNameValue("outstandingBalance", nameFinderQueryResponse.getData().getUser().getOutstandingBalance()));
            customerValidationResponse.getData().add(new ParamNameValue("dueDate", nameFinderQueryResponse.getData().getUser().getDueDate()));
            return customerValidationResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private CustomerValidationResponse validateSpectranet(CustomerValidationRequest request) throws ThirdPartyIntegrationException {

        List<ParamNameValue> paramNameValues = request.getData();
        DataBundleRequest request1 = new DataBundleRequest();
        for (ParamNameValue data : paramNameValues){
            if (ACCOUNT_NUMBER.equals(data.getName())){
                request1.setAccountNumber(data.getValue());
            }
        }
        request1.setServiceType(request.getBillerId());

        Optional<DataBundleResponse> bundleResponseOptional = Optional.empty();
        try {
            bundleResponseOptional = Optional.of(feignClient.getDataBundles(appConfig.getBaxi().getXApiKey(), request1));
        } catch (FeignException e) {
            log.error("Unable to verify customer Spectranet account number via Baxi ", e);
        }
        DataBundleResponse dataBundleResponse = bundleResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch data bundles"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(request.getCategoryId(), request.getBillerId());
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item(PHONE));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemBundles = new Item();
        itemBundles.setParamName("bundles");
        itemBundles.setIsAmountFixed(Boolean.TRUE);
        dataBundleResponse.getData().forEach(dataBundle -> itemBundles.getSubItems().add(new SubItem(dataBundle.getDatacode(), dataBundle.getName(), dataBundle.getPrice(), dataBundle.getPrice())));
        paymentItemsResponse.getItems().add(itemBundles);
//        if(SUCCESS_RESPONSE_CODE.equals(dataBundleResponse.getCode()) && !Objects.isNull(dataBundleResponse.getData())) {
//            CustomerValidationResponse customerValidationResponse = new CustomerValidationResponse(request.getCategoryId(), request.getBillerId());
//            customerValidationResponse.getData().add(new ParamNameValue("name", nameFinderQueryResponse.getData().getUser().getName()));
//            customerValidationResponse.getData().add(new ParamNameValue("outstandingBalance", nameFinderQueryResponse.getData().getUser().getOutstandingBalance()));
//            customerValidationResponse.getData().add(new ParamNameValue("dueDate", nameFinderQueryResponse.getData().getUser().getDueDate()));
//            return customerValidationResponse;
//        }
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }


    private void processValidation(CustomerValidationRequest request, DataBundleRequest request1) throws ThirdPartyIntegrationException {
        Optional<DataBundleResponse> bundleResponseOptional = Optional.empty();
        try {
            bundleResponseOptional = Optional.of(feignClient.getDataBundles(appConfig.getBaxi().getXApiKey(), request1));
        } catch (FeignException e) {
            log.error("Unable to verify customer Spectranet account number via Baxi ", e);
        }
        DataBundleResponse dataBundleResponse = bundleResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch data bundles"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(request.getCategoryId(), request.getBillerId());
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item(PHONE));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemBundles = new Item();
        itemBundles.setParamName("bundles");
        itemBundles.setIsAmountFixed(Boolean.TRUE);
        dataBundleResponse.getData().forEach(dataBundle -> itemBundles.getSubItems().add(new SubItem(dataBundle.getDatacode(), dataBundle.getName(), dataBundle.getPrice(), dataBundle.getPrice())));
        paymentItemsResponse.getItems().add(itemBundles);

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private CustomerValidationResponse validateBetting(CustomerValidationRequest request) throws ThirdPartyIntegrationException {

        List<ParamNameValue> paramNameValues = request.getData();
        DataBundleRequest request1 = new DataBundleRequest();
        for (ParamNameValue data : paramNameValues){
            if (ACCOUNT_NUMBER.equals(data.getName())){
                request1.setAccountNumber(data.getValue());
            }
        }
        request1.setServiceType(request.getBillerId());

        System.out.println("validateSpectranet :: " + request1);

        Optional<DataBundleResponse> bundleResponseOptional = Optional.empty();
        try {
            bundleResponseOptional = Optional.of(feignClient.getDataBundles(appConfig.getBaxi().getXApiKey(), request1));
        } catch (FeignException e) {
            log.error("Unable to verify customer Spectranet account number via Baxi ", e);
        }
        DataBundleResponse dataBundleResponse = bundleResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch data bundles"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(request.getCategoryId(), request.getBillerId());
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item(PHONE));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemBundles = new Item();
        itemBundles.setParamName("bundles");
        itemBundles.setIsAmountFixed(Boolean.TRUE);
        dataBundleResponse.getData().forEach(dataBundle -> itemBundles.getSubItems().add(new SubItem(dataBundle.getDatacode(), dataBundle.getName(), dataBundle.getPrice(), dataBundle.getPrice())));
        paymentItemsResponse.getItems().add(itemBundles);

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    //

    private CustomerValidationResponse validateElectricity(CustomerValidationRequest request) throws ThirdPartyIntegrationException {
        ElectricityRequest electricityRequest = generateElectricityRequest(request);
        Optional<ElectricityVerificationResponse> electricityVerificationResponseOptional = Optional.empty();
        try {
            electricityVerificationResponseOptional = Optional.of(feignClient.verifyCustomerElectricityDetail(appConfig.getBaxi().getXApiKey(), electricityRequest));

        } catch (FeignException e) {
            log.error("Unable to verify customer electricity payment via Baxi ", e);
        }

        ElectricityVerificationResponse electricityVerificationResponse = electricityVerificationResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.BAXI_VALIDATATION));
        if(SUCCESS_RESPONSE_CODE.equals(electricityVerificationResponse.getCode()) && !Objects.isNull(electricityVerificationResponse.getData())) {
            CustomerValidationResponse customerValidationResponse = new CustomerValidationResponse(request.getCategoryId(), request.getBillerId());
            customerValidationResponse.getData().add(new ParamNameValue("name", electricityVerificationResponse.getData().getName()));
            customerValidationResponse.getData().add(new ParamNameValue("address", electricityVerificationResponse.getData().getAddress()));
            customerValidationResponse.getData().add(new ParamNameValue("district", electricityVerificationResponse.getData().getDistrict()));
            customerValidationResponse.getItems().add(new Item(PHONE));
            customerValidationResponse.getItems().add(new Item(AMOUNT));
            return customerValidationResponse;
        }

        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private ElectricityRequest generateElectricityRequest(CustomerValidationRequest request) throws ThirdPartyIntegrationException {
        ParamNameValue accountNumberParamValue = request.getData().stream().filter(paramNameValue -> paramNameValue.getName().equals(ACCOUNT_NUMBER)).findFirst().orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Account number is required"));
        ElectricityRequest electricityRequest = new ElectricityRequest();
        electricityRequest.setAccount_number(accountNumberParamValue.getValue());
        electricityRequest.setService_type(request.getBillerId());
        return electricityRequest;
    }

    private PaymentItemsResponse getAirtimePaymentItems(String categoryId, String billerId){
        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item(PHONE));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemPlan = new Item();
        itemPlan.setParamName("plan");
        itemPlan.getSubItems().add(new SubItem("prepaid"));
        itemPlan.getSubItems().add(new SubItem("postpaid"));
        paymentItemsResponse.getItems().add(itemPlan);
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getDataBundlePaymentItems(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        Optional<DataBundleResponse> bundleResponseOptional = Optional.empty();
        try {
            DataBundleRequest request = new DataBundleRequest();
            request.setServiceType(billerId);

            bundleResponseOptional = Optional.of(feignClient.getDataBundles(appConfig.getBaxi().getXApiKey(), request));
        } catch (FeignException e) {
            log.error("Unable to fetch Data Bundles", e);
        }
        DataBundleResponse dataBundleResponse = bundleResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch data bundles"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item(PHONE));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemBundles = new Item();
        itemBundles.setParamName("bundles");
        itemBundles.setIsAmountFixed(Boolean.TRUE);
        dataBundleResponse.getData().forEach(dataBundle -> itemBundles.getSubItems().add(new SubItem(dataBundle.getDatacode(), dataBundle.getName(), dataBundle.getPrice(), dataBundle.getPrice())));
        paymentItemsResponse.getItems().add(itemBundles);

        return paymentItemsResponse;
    }

    private PaymentItemsResponse getSpectranetBundlePaymentItems(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        Optional<DataBundleResponse> bundleResponseOptional = Optional.empty();
        try {
            DataBundleRequest request = new DataBundleRequest();
            request.setServiceType(billerId);

            bundleResponseOptional = Optional.of(feignClient.getDataBundles(appConfig.getBaxi().getXApiKey(), request));
        } catch (FeignException e) {
            log.error("Unable to fetch Data Bundles", e);
        }
        DataBundleResponse dataBundleResponse = bundleResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch data bundles"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item(PHONE));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemBundles = new Item();
        itemBundles.setParamName("bundles");
        itemBundles.setIsAmountFixed(Boolean.TRUE);
        dataBundleResponse.getData().forEach(dataBundle -> itemBundles.getSubItems().add(new SubItem(dataBundle.getDatacode(), dataBundle.getName(), dataBundle.getPrice(), dataBundle.getPrice())));
        paymentItemsResponse.getItems().add(itemBundles);

        return paymentItemsResponse;
    }


    private PaymentItemsResponse getCableTvPaymentItems(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        Optional<CableTvPlanResponse> cableTvPlanResponseOptional = Optional.empty();
        try {
            CableTvRequest request = new CableTvRequest();
            request.setServiceType(billerId);
            cableTvPlanResponseOptional = Optional.of(feignClient.getCableTvPlans(appConfig.getBaxi().getXApiKey(), request));
        } catch (FeignException e) {
            log.error("Unable to fetch Cable Tv Plans",e );
        }
        CableTvPlanResponse cableTvPlanResponse = cableTvPlanResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch cable tv plans"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item("smartcard_number"));
        paymentItemsResponse.getItems().add(new Item("total_amount"));
        Item itemPlan = new Item();
        itemPlan.setParamName("plan");
        itemPlan.setIsAmountFixed(Boolean.TRUE);
        cableTvPlanResponse.getData().forEach(plan -> {
            Optional<Pricing> pricingOptional = plan.getAvailablePricingOptions().stream().filter(pricing -> pricing.getMonthsPaidFor().equals("1")).findFirst();
            String price = pricingOptional.orElse(new Pricing("0")).getPrice();
            itemPlan.getSubItems().add(new SubItem(plan.getCode(), plan.getName(), price, price));
        });
        paymentItemsResponse.getItems().add(itemPlan);
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getElectricityPaymentItems(String categoryId, String billerId){
        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item(ACCOUNT_NUMBER));
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getEPinBundlePaymentItems(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        Optional<EPinBundleResponse> ePinBundleResponseOptional = Optional.empty();
        try {
            ePinBundleResponseOptional = Optional.of(feignClient.getEpinBundles(appConfig.getBaxi().getXApiKey(), billerId));
        } catch (FeignException e) {
            log.error("Unable to fetch Epin Bundles", e);
        }
        EPinBundleResponse ePinBundleResponse = ePinBundleResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch EPin bundles"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item("numberOfPins"));
        Item itemPinValues = new Item();
        itemPinValues.setIsAmountFixed(Boolean.TRUE);
        itemPinValues.setParamName("pinValue");
        ePinBundleResponse.getData().forEach(ePinBundle -> itemPinValues.getSubItems().add(new SubItem(ePinBundle.getAmount(), ePinBundle.getDescription(), ePinBundle.getAmount(), ePinBundle.getAmount())));
        paymentItemsResponse.getItems().add(itemPinValues);
        return paymentItemsResponse;
    }

    private String getErrorMessage(String errorInJson){
        try {
            return CommonUtils.getObjectMapper().readValue(errorInJson, ErrorResponse.class).getMessage();
        } catch (JsonProcessingException e) {
            log.error("[JsonProcessingException] : Unable to ", e);
            return Constants.ERROR_MESSAGE;
        }
    }
    private String getCustomRefCode(String transactionId){
        return "4352"+transactionId;
    }
}

class BillerCategoryName{
    static final String AIRTIME = "airtime";
    static final String DATABUNDLE = "databundle";
    static final String SPECTRANET = "spectranet";
    static final String CABLETV = "cabletv";
    static final String ELECTRICITY = "electricity";
    static final String BETTING = "betting";
    static final String INSURANCE = "insurance";
    static final String EPIN = "epin";
    static final String BAXI = "BAXI";

    private BillerCategoryName(){

    }
}

class BaxiConstants{
    static final String X_API_KEY = "x-api-key";
}

@Getter
@Setter
@NoArgsConstructor
class ErrorResponse{
    private String status;
    private String message;
    private String code;
}