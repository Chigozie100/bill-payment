package com.wayapay.thirdpartyintegrationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.commission.*;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CommissionOperationService {
    private final CommissionFeignClient commissionFeignClient;
    private final WalletFeignClient walletFeignClient;
    private final NotificationService notificationService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AppConfig appConfig;
    private final TokenImpl tokenImpl;

    @Autowired
    public CommissionOperationService(CommissionFeignClient commissionFeignClient, WalletFeignClient walletFeignClient, NotificationService notificationService, KafkaTemplate<String, String> kafkaTemplate, AppConfig appConfig,TokenImpl tokenImpl) {
        this.commissionFeignClient = commissionFeignClient;
        this.walletFeignClient = walletFeignClient;
        this.notificationService = notificationService;
        this.kafkaTemplate = kafkaTemplate;
        this.appConfig = appConfig;
        this.tokenImpl = tokenImpl;
    }

    public UserCommissionDto findUserCommission(UserType userType, String token, String userId, String biller_id, String categoryCode) throws ThirdPartyIntegrationException {

        try {
            log.info("Response userId" + userId);
            log.info("Response biller_id" + biller_id);
            log.info("Response categoryCode" + categoryCode);
   
            ResponseEntity<ApiResponseBody<BillspaymentCommissionRespones>> response = commissionFeignClient.getBillspaymentCommission(Long.parseLong(userId), biller_id,categoryCode,token);
            ApiResponseBody<BillspaymentCommissionRespones> infoResponse2 = response.getBody();
            BillspaymentCommissionRespones userBillsCommissionDto = infoResponse2 != null ? infoResponse2.getData() : null;
            log.info("Response from Commission Service" + response);
            log.info("Response userBillsCommissionDto" + userBillsCommissionDto);

            UserCommissionDto userCommissionDTO = new UserCommissionDto(userBillsCommissionDto.getId(), UserType.ROLE_USER_MERCHANT, userBillsCommissionDto.getStatus(), TransactionType.BILLS_PAYMENT,
            CommissionType.valueOf(userBillsCommissionDto.getCommissionType()), userBillsCommissionDto.getAmount(), BigDecimal.valueOf(0.50), true
            );
 
            // ResponseEntity<ApiResponseBody<UserCommissionDto>> response = commissionFeignClient.userCommissionExtra(userType, TransactionType.BILLS_PAYMENT,token);
            // ApiResponseBody<UserCommissionDto> infoResponse = response.getBody();
            // UserCommissionDto userCommissionDto = infoResponse != null ? infoResponse.getData() : null;

             log.info("Response userCommissionDTO" + userCommissionDTO);
            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }

           return userCommissionDTO;

        } catch (RestClientException e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }

    public String getOfficialAccount(String eventId, String token) throws ThirdPartyIntegrationException {

        try { 
  
            ResponseEntity<ApiResponseBody<?>> response = walletFeignClient.officialAccount(eventId,token);
            ApiResponseBody<?> infoResponse =  response.getBody();

            log.info("Response from Commission Service" + infoResponse.getData());
            log.info("Response from Commission Service 2" + infoResponse);
            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }
            if(infoResponse.getData() !=null){
                return infoResponse.getData().toString();
            }
            return null; 
        } catch (RestClientException e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }

    private BigDecimal computePercentage(BigDecimal amount, BigDecimal percentageValue){
        BigDecimal per = BigDecimal.valueOf(percentageValue.doubleValue() / 100);
        return BigDecimal.valueOf(per.doubleValue() * amount.doubleValue());
    }

     //WAYA (Mifos) QUICKTELLER BILLS PAYMENT COMMISSION RECEIVABLE ACCOUNT Debited WITH 40% of 5% Commission Receivable less 4% Commission payable which is N500 - N400 = N100 - 40% = N40

    private BigDecimal computeAggregatorPercentage(BigDecimal amount){
        double answer = ((amount.doubleValue() * 5.0) / 100);
        double fourPercentAnswer = ((answer * 40) / 100);
        return BigDecimal.valueOf(answer - fourPercentAnswer);
    }
 
    public void payUserCommission(UserType userType,String userId, String token, BigDecimal amount, String eventId, String thirdParty, String billerId, String categoryId) throws ThirdPartyIntegrationException {
   
       String systemToken = tokenImpl.getToken();
       String systemPin = tokenImpl.getPin();
       log.info("  systemToken " + systemToken); 

        // call offical account
        String officialAccount = getOfficialAccount(eventId, systemToken);
        String commissionSettlement = getOfficialAccount(thirdParty, systemToken);

        UserCommissionDto userCommissionDto = findUserCommission(userType,token,userId,billerId,categoryId);

        BigDecimal amountr = computePercentage(amount,userCommissionDto.getCommissionValue());
        log.info("  amountr " + amountr);
        OfficalToOfficial transfer =  new OfficalToOfficial(amountr,
        commissionSettlement,officialAccount,String.valueOf(CommonUtils.generatePaymentTransactionId()),
        Constants.NGN,Constants.LOCAL,Constants.COMMISSION_PAYMENT_TRANSACTION);
        transfer.setAmount(amountr);


        log.info("Billspyament::  Merchant Commission Amount for buying billspayment " + transfer);

        ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>>  responseEntity = walletFeignClient.officialToOfficial(transfer,systemToken, systemPin);
        ApiResponseBody<List<WalletTransactionPojo>> infoResponse = responseEntity.getBody();

        log.info("Billspyament::  Merchant Commission Amount for buying billspayment RESPONSE::" + infoResponse);

        // List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
        // List<WalletTransactionPojo> walletTransactionPojoList = new ArrayList<>(Objects.requireNonNull(mainWalletResponseList));

        logRequest(infoResponse, amount, transfer.getOfficeCreditAccount(), eventId,  transfer.getPaymentReference(),  transfer.getTranCrncy(),  transfer.getTranNarration(), Constants.COMMISSION, userId, userType, token);

        // TransferFromWalletPojo transfer2 = new TransferFromWalletPojo(amount, transfer.getCustomerCreditAccount(), null,  transfer.getPaymentReference(),  transfer.getTranCrncy(),  transfer.getTranNarration(), transfer.getTransactionCategory(), Long.valueOf(userId));
        // saveCommissionHistory(userId,transfer2, walletTransactionPojoList,userType,token);
        // inAppNotification(userId, transfer2, walletTransactionPojoList, token, infoResponse);

        // WAYA (Mifos) Quickteller BILLS PAYMENT COMMISSION RECEIVABLE ACCOUNT Debited WITH 4% (N400) 
        log.info("### About to debit commission settlement account and credit user commission account ###");
        NewWalletResponse userCommissionWallet = getUserCommissionWallet(userId,token); // get user commission wallet
        OfficialToUserCommission transfer2 =  new OfficialToUserCommission(amountr,userCommissionWallet.getAccountNo(),commissionSettlement,String.valueOf(CommonUtils.generatePaymentTransactionId()),
        Constants.NGN,Constants.LOCAL,Constants.COMMISSION_PAYMENT_TRANSACTION,Constants.COMMISSION);
        transfer2.setAmount(amountr);
        //WAYA (Mifos) Quickteller Bills Payment Commission Settlement Account Credited with N400
        ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>>  responseEntity2 = walletFeignClient.officialToUserCommission(transfer2, systemToken, systemPin);
        ApiResponseBody<List<WalletTransactionPojo>> infoResponse2 = responseEntity2.getBody();
        logRequest(infoResponse2, amount, transfer2.getCustomerCreditAccount(), eventId,  transfer2.getPaymentReference(),  transfer2.getTranCrncy(),  transfer2.getTranNarration(), transfer2.getTransactionCategory(), userId, userType, token);

    }

    private void logRequest(ApiResponseBody<List<WalletTransactionPojo>> infoResponse, BigDecimal amount, String  getCustomerCreditAccount, String eventId, String getPaymentReference,  String getTranCrncy,  String  getTranNarration, String getTransactionCategory, String userId, UserType userType, String token){
        List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
        List<WalletTransactionPojo> walletTransactionPojoList = new ArrayList<>(Objects.requireNonNull(mainWalletResponseList));

        TransferFromWalletPojo transfer2 = new TransferFromWalletPojo(amount, getCustomerCreditAccount, null, getPaymentReference,  getTranCrncy, getTranNarration, getTransactionCategory, Long.valueOf(userId));
        saveCommissionHistory(userId,transfer2, walletTransactionPojoList,userType,token);
        inAppNotification(userId, transfer2, walletTransactionPojoList, token, infoResponse);

    }

    public void payOrganisationCommission(UserType userType,String billerId,String userId, String token, BigDecimal amount, String eventId, String categoryId) throws ThirdPartyIntegrationException {
        log.info("Billspyament:: in here start payOrganisationCommission after payment ::: ");
        String systemToken = tokenImpl.getToken();
        String systemPin = tokenImpl.getPin();
        log.info("  systemToken " + systemToken); 
        log.info("  userId " + userId); 
        log.info("  billerId " + billerId); 
 
        String officialAccount = getOfficialAccount(eventId, systemToken); 
        NewWalletResponse userCommissionWallet = getUserCommissionWallet(userId,token); // get user commission wallet

        BigDecimal amountr = computeAggregatorPercentage(amount);
        log.info("  amountr " + amountr);
        OfficialToUserCommission transfer =  new OfficialToUserCommission(amountr,userCommissionWallet.getAccountNo(),officialAccount,String.valueOf(CommonUtils.generatePaymentTransactionId()),
        Constants.NGN,Constants.LOCAL,Constants.COMMISSION_PAYMENT_TRANSACTION,Constants.COMMISSION);
        transfer.setAmount(amountr);

        
        log.info("Billspyament::  Merchant Commission Amount for selling billspayment " + transfer);
        ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>>  responseEntity = walletFeignClient.officialToUserCommission(transfer,systemToken,systemPin);
        ApiResponseBody<List<WalletTransactionPojo>> infoResponse = responseEntity.getBody();

        List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
        List<WalletTransactionPojo> walletTransactionPojoList = new ArrayList<>(Objects.requireNonNull(mainWalletResponseList));
        log.info("Billspayment:: in here payOrganisationCommission after payment ::: RESPONSE" + walletTransactionPojoList);

        TransferFromWalletPojo transfer3 = new TransferFromWalletPojo();
        
        transfer3.setAmount(transfer.getAmount());
        transfer3.setCustomerAccountNumber(officialAccount);
        transfer3.setEventId(eventId);
        transfer3.setTranCrncy("NGN");
        transfer3.setPaymentReference(String.valueOf(CommonUtils.generatePaymentTransactionId()));
        transfer3.setTranNarration("AGGREGATOR-COMMISSION-PAYMENT");
        transfer3.setTransactionCategory("COMMISSION");
        saveCommissionHistory(userId,transfer3, walletTransactionPojoList,userType,token);
 
        inAppNotification(billerId, transfer3, walletTransactionPojoList, token, infoResponse); 
        log.info("Billspyament::  in here payOrganisationCommission ::: ");

    }

    private NewWalletResponse getUserCommissionWallet(String userId, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ApiResponseBody<NewWalletResponse>> commissionWallet = walletFeignClient.getUserCommissionWallet(userId,token);
            ApiResponseBody<NewWalletResponse> commissionWalletBody = commissionWallet.getBody();
            log.info("Billspyament::  in here getUserCommissionWallet ::: "+ commissionWalletBody);
            return commissionWalletBody != null ? commissionWalletBody.getData() : null;
        }catch (Exception exception){
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, exception.getMessage());
        }

    }

    public void saveMerchantCommission(MerchantCommissionTrackerDto request, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ApiResponseBody<MerchantCommissionTrackerDto>> responseEntity = commissionFeignClient.recordMerchantCommission(request,token);
            ApiResponseBody<MerchantCommissionTrackerDto> responseBody = responseEntity.getBody();
            log.info("Billspyament::  in here saveMerchantCommission ::: " + Objects.requireNonNull(responseBody).getData());

        }catch (Exception exception){
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, exception.getMessage());
        }

    }


    private OrganisationCommissionResponse getOrgCommission(String biller, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ApiResponseBody<OrganisationCommissionResponse>> responseEntity = commissionFeignClient.getOrgCommission(biller,token);
            ApiResponseBody<OrganisationCommissionResponse> responseBody = responseEntity.getBody();
            log.info("Billspyament::  in here saveMerchantCommission ::: " + Objects.requireNonNull(responseBody).getData());
            return responseBody.getData();
        }catch (Exception exception){
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, exception.getMessage());
        }
    }



    private void saveCommissionHistory(String userId,TransferFromWalletPojo transfer, List<WalletTransactionPojo> walletTransactionPojoList, UserType userType, String token){
        CompletableFuture.runAsync(() -> {
            CommissionHistoryRequest commissionDto = new CommissionHistoryRequest();

            commissionDto.setUserId(userId);
            commissionDto.setCommissionValue(transfer.getAmount());
            commissionDto.setJsonRequest(CommonUtils.objectToJson(transfer).orElse(""));
            commissionDto.setJsonResponse(CommonUtils.objectToJson(walletTransactionPojoList).orElse(""));
            commissionDto.setTransactionType(TransactionType.BILLS_PAYMENT);
            commissionDto.setUserType(userType);

            ResponseEntity<ApiResponseBody<CommissionDto>> resp = commissionFeignClient.addCommissionHistory(commissionDto, token);
            ApiResponseBody<CommissionDto> commissionDtoApiResponseBody = resp.getBody();
            log.info(String.format("Bills-payment: payUserCommission response :: {} %s", commissionDtoApiResponseBody != null ? commissionDtoApiResponseBody.getData() : null));
        });

    }

    private void inAppNotification(String userId, TransferFromWalletPojo transfer, List<WalletTransactionPojo> walletTransactionPojoList, String token, ApiResponseBody<List<WalletTransactionPojo>> infoResponse){
        List<String> inAppRecipient = new ArrayList<>();
        inAppRecipient.add(userId);

        Map<String, String> dto = new HashMap<>();
        dto.put("userId",userId);
        dto.put("ref", transfer.getPaymentReference());
        dto.put("amount", transfer.getAmount().toString());
        dto.put("sender", "WAYA-ADMIN");
        dto.put("initiator", userId);
        dto.put("category", "BILLS-PAYMENT-COMMISSION");
        dto.put("in_app_recipient", inAppRecipient.toString());
        log.info(walletTransactionPojoList.toString());


        if (infoResponse.getStatus()){
            dto.put("message", "Fund Merchant Commission Wallet Successful");
        }else{
            dto.put("message", "Error Funding Merchant Commission Wallet");
        }

        CompletableFuture.runAsync(() -> {
            try {
                notificationService.pushINAPP(dto, token);
            } catch (ThirdPartyIntegrationException e) {
                e.printStackTrace();
            }
        });
    }

    private void emailNotification(OrganisationCommissionResponse response, String userId, TransferFromWalletPojo transfer, List<WalletTransactionPojo> walletTransactionPojoList, String token, ApiResponseBody<List<WalletTransactionPojo>> infoResponse) throws ThirdPartyIntegrationException {

        Map<String, String> map = new HashMap<>();

        map.put("paymentTransactionAmount", transfer.getAmount().toString());
        map.put("userId", response.getCorporateUserId());
        map.put("email", response.getCorporateUserEmail());
        map.put("phoneNumber", response.getCorporateUserPhoneNumber());
        map.put("surname", response.getCorporateUserId());
        map.put("firstName", response.getMerchantName());
        map.put("middleName", response.getMerchantName());
        map.put("transactionId", transfer.getPaymentReference());
        map.put("amount", transfer.getAmount().toString());
        if (infoResponse.getStatus()){
            map.put("message", "Fund Merchant Commission Wallet Successful");
        }else{
            map.put("message", "Error Funding Merchant Commission Wallet");
        }

        log.info(userId+walletTransactionPojoList.toString());

        notificationService.pushEMAIL(map, token);
    }


    public void pushToCommissionService(Map<String, Object> map) throws JsonProcessingException {
        kafkaTemplate.send(appConfig.getKafka().getSellBillsTopic(),  CommonUtils.getObjectMapper().writeValueAsString(map));
    }

}
