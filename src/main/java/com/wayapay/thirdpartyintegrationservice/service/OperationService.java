package com.wayapay.thirdpartyintegrationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.BillsPaymentRefund;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.model.TransactionTracker;
import com.wayapay.thirdpartyintegrationservice.repo.BillsPaymentRefundRepository;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.repo.TransactionTrackerRepository;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseObj;
import com.wayapay.thirdpartyintegrationservice.service.logactivity.LogFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.logactivity.LogRequest;
import com.wayapay.thirdpartyintegrationservice.service.notification.*;
import com.wayapay.thirdpartyintegrationservice.service.profile.ProfileFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;
import com.wayapay.thirdpartyintegrationservice.service.referral.ReferralCodePojo;
import com.wayapay.thirdpartyintegrationservice.service.referral.ReferralFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.wallet.FundTransferResponse;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationService {

    private final PaymentTransactionRepo paymentTransactionRepo;
    private final WalletFeignClient walletFeignClient;
    private final CategoryService categoryService;
    private final ProfileFeignClient profileFeignClient;
    private final NotificationFeignClient notificationFeignClient;
    private final LogFeignClient logFeignClient;
    private final BillsPaymentRefundRepository billsPaymentRefundRepository;
    private final TransactionTrackerRepository transactionTrackerRepository;
    private final ReferralFeignClient referralFeignClient;
    private final KafkaTemplate<String, String> kafkaTemplate;


    public UserProfileResponse getUserProfile(String userName, String token) throws ThirdPartyIntegrationException {
        UserProfileResponse userProfileResponse = null;
        try {
            ResponseEntity<ProfileResponseObject> responseEntity = profileFeignClient.getUserProfile(userName, token);
            ProfileResponseObject infoResponse =  responseEntity.getBody();
            userProfileResponse = infoResponse.data;
            log.info("userProfileResponse :: " +userProfileResponse);
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
        return userProfileResponse;
    }

    public LogRequest creatLog(LogRequest logRequest, String token) throws ThirdPartyIntegrationException {

        try {
            ResponseEntity<ResponseObj<LogRequest>> response = logFeignClient.createLog(logRequest,token);

            ResponseObj infoResponse = response.getBody();
            LogRequest logRequest1 = (LogRequest) infoResponse.data;
            log.info("ResponseObj :: " +infoResponse.data);
            return logRequest1;

        } catch (RestClientException e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }

    public void logUserActivity(PaymentRequest request, Map<String,String> map, String token) throws ThirdPartyIntegrationException {
        Map<String,Object> mapp = new HashMap<>();
        mapp.put("amount", request.getAmount());
        mapp.put("billerId", request.getBillerId());
        mapp.put("categoryId", request.getCategoryId());
        mapp.put("sourceWalletAccountNumber", request.getSourceWalletAccountNumber());

        List<Map<String,Object>> newList = new ArrayList<>();
        for (int i = 0; i < request.getData().size(); i++) {
            Map<String,Object> maaapp = new HashMap<>();
            String value = request.getData().get(i).getValue();
            maaapp.put("value", value);
            String name = request.getData().get(i).getName();
            maaapp.put("value", name);
            newList.add(maaapp);
        }
        mapp.put("ParamNameValue", newList);

        log.info("about to log....");
        LogRequest logRequest = new LogRequest();
        logRequest.setAction("CREATE");
        logRequest.setJsonRequest(mapp.toString());
        logRequest.setModule(map.get("module"));
        logRequest.setRequestDate(new Date());
        logRequest.setUserId(Long.valueOf(map.get("userId")));
        logRequest.setMessage(map.get("message"));
        creatLog(logRequest, token);
        log.info("done logging....");

    }

    public void logUserActivity(MultiplePaymentRequest request, Map<String,String> map, String token) throws ThirdPartyIntegrationException {
        Map<String, Object> mapp = getStringObjectMap(request);

        List<Map<String,Object>> newList = new ArrayList<>();
        for (int i = 0; i < request.getData().size(); i++) {
            Map<String,Object> maaapp = new HashMap<>();
            String value = request.getData().get(i).getValue();
            maaapp.put("value", value);
            String name = request.getData().get(i).getName();
            maaapp.put("value", name);
            newList.add(maaapp);
        }
        mapp.put("ParamNameValue", newList);

        log.info("about to log....");
        LogRequest logRequest = new LogRequest();
        logRequest.setAction("CREATE");
        logRequest.setJsonRequest(mapp.toString());
        logRequest.setModule(map.get("module"));
        logRequest.setRequestDate(new Date());
        logRequest.setUserId(Long.valueOf(map.get("userId")));
        logRequest.setMessage(map.get("message"));
        creatLog(logRequest, token);
        log.info("done logging....");

    }

    private Map<String, Object> getStringObjectMap(MultiplePaymentRequest request) {
        Map<String,Object> mapp = new HashMap<>();
        mapp.put("amount", request.getAmount());
        mapp.put("billerId", request.getBillerId());
        mapp.put("categoryId", request.getCategoryId());
        mapp.put("sourceWalletAccountNumber", request.getSourceWalletAccountNumber());
        return mapp;
    }

    public void sendInAppNotification(InAppEvent inAppEvent, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ResponseObj>  responseEntity = notificationFeignClient.inAppNotifyUser(inAppEvent,token);
            ResponseObj infoResponse = (ResponseObj) responseEntity.getBody();
            log.info("userProfileResponse :: " +infoResponse.data);
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }
    public void sendEmailNotification(EmailEvent emailEvent, String token) throws ThirdPartyIntegrationException {

        try {
            ResponseEntity<ResponseObj>  responseEntity = notificationFeignClient.emailNotifyUser(emailEvent,token);
            ResponseObj infoResponse = (ResponseObj) responseEntity.getBody();
            log.info("userProfileResponse email sent status :: " +infoResponse.status);
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }

    private SMSGatewayResponse getActiveSMSGateway(String token) throws ThirdPartyIntegrationException {
        SMSGatewayResponse smsGatewayResponse = null;
        try {
            ResponseEntity<ResponseObj<SMSGatewayResponse>>  responseEntity = notificationFeignClient.getActiveSMSGateway(token);
            ResponseObj infoResponse = (ResponseObj) responseEntity.getBody();
            smsGatewayResponse = (SMSGatewayResponse) infoResponse.data;
            log.info("getActiveSMSGateway   :: " +smsGatewayResponse);
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
        return smsGatewayResponse;
    }

//    @Async("threadPoolTaskExecutor")
//    public CompletableFuture<Boolean>  smsNotification(SmsEvent smsEvent, String token) throws ThirdPartyIntegrationException {
//
//
//        //String checkSMSGateway = getActiveSMSGateway(token).getName();
//        try {
//
////        switch(checkSMSGateway) {
////            case "ATALKING":
////                responseEntity = notificationFeignClient.smsNotifyUserAtalking(smsEvent,token);
////                break;
////            case "INFOBIP":
////                responseEntity = notificationFeignClient.smsNotifyUserInfobip(smsEvent,token);
////                break;
////            case "TWILIO":
////                responseEntity = notificationFeignClient.smsNotifyUserTwilio(smsEvent,token);
////                break;
////            default:
////                responseEntity = notificationFeignClient.smsNotifyUserTwilio(smsEvent,token);
////        }
//            ResponseEntity<ResponseObj>  responseEntity = notificationFeignClient.smsNotifyUser(smsEvent,token);
//
//            ResponseObj infoResponse = responseEntity.getBody();
//            log.info("userProfileResponse sms sent status :: " +infoResponse.status);
//            return CompletableFuture.completedFuture(infoResponse.status);
//        } catch (Exception e) {
//            log.error("Unable to generate transaction Id", e);
//            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
//        }
//
//    }


    public SMSChargeResponse getSMSCharges(String token) throws ThirdPartyIntegrationException {

        try {
            ResponseEntity<ResponseObj<SMSChargeResponse>>  responseEntity = notificationFeignClient.getActiveSMSCharge(token);
            ResponseObj infoResponse = (ResponseObj) responseEntity.getBody();
            SMSChargeResponse smsChargeResponse = (SMSChargeResponse) infoResponse.data;
            return smsChargeResponse;

        } catch (RestClientException e) {
            log.info(e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

    }

    @AuditPaymentOperation(stage = Stage.SECURE_FUND, status = Status.START)
    public boolean secureFund(BigDecimal amount, BigDecimal fee, String userName, String userAccountNumber, String transactionId, FeeBearer feeBearer, String token, String billType) throws ThirdPartyIntegrationException {
        //Get user default wallet

        ResponseEntity<InfoResponse> responseEntity = walletFeignClient.getDefaultWallet(userName, token);
        InfoResponse infoResponse = responseEntity.getBody();
        NewWalletResponse mainWalletResponse = infoResponse.data;
        log.info("mainWalletResponse:: " + mainWalletResponse);

        if (mainWalletResponse.getClr_bal_amt().doubleValue() < amount.doubleValue())
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.INSUFFICIENT_FUND);

        //consume
        TransferFromWalletPojo trans = new TransferFromWalletPojo();
        trans.setAmount(FeeBearer.CONSUMER.equals(feeBearer) ? amount.add(fee) : amount);

        trans.setCustomerAccountNumber(mainWalletResponse.getAccountNo());
        trans.setEventId(EventCharges.AITCOL.name());
        trans.setPaymentReference(transactionId);
        trans.setTranCrncy("NGN");
        trans.setTransactionCategory(billType);
        trans.setTranNarration(TransactionType.BILLS_PAYMENT.name());
        trans.setUserId(Long.parseLong(userName));
        try {
            walletFeignClient.transferFromUserToWaya(trans,token);
            return true;
        } catch (FeignException exception) {
            log.error("FeignException => {}", exception.getCause());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getErrorMessage(exception.contentUTF8()));
        }
    }



    @AuditPaymentOperation(stage = Stage.SAVE_TRANSACTION_DETAIL, status = Status.END)
    public PaymentTransactionDetail saveTransactionDetail(UserProfileResponse userProfileResponse, PaymentRequest paymentRequest, BigDecimal fee, PaymentResponse paymentResponse, String userName, String transactionId) throws ThirdPartyIntegrationException {

        PaymentTransactionDetail paymentTransactionDetail = new PaymentTransactionDetail();
        paymentTransactionDetail.setAmount(paymentRequest.getAmount());
        paymentTransactionDetail.setFee(fee);
        paymentTransactionDetail.setBiller(paymentRequest.getBillerId());
        paymentTransactionDetail.setCategory(paymentRequest.getCategoryId());
        paymentTransactionDetail.setPaymentRequest(CommonUtils.objectToJson(paymentRequest).orElse(""));
        paymentTransactionDetail.setPaymentResponse(CommonUtils.objectToJson(paymentResponse).orElse(""));
        paymentTransactionDetail.setSuccessful(true);
        paymentTransactionDetail.setThirdPartyName(categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE)));
        paymentTransactionDetail.setTransactionId(transactionId);
        paymentTransactionDetail.setUserAccountNumber(paymentRequest.getSourceWalletAccountNumber());
        paymentTransactionDetail.setUsername(userName);
        paymentTransactionDetail.setReferralCode(userProfileResponse.getReferral());
        paymentTransactionDetail.setPhoneNumber(userProfileResponse.getPhoneNumber());
        paymentTransactionDetail.setEmail(userProfileResponse.getEmail());
        return paymentTransactionRepo.save(paymentTransactionDetail);
    }

    @AuditPaymentOperation(stage = Stage.SAVE_TRANSACTION_DETAIL, status = Status.END)
    public PaymentTransactionDetail saveTransactionDetailMultiple(UserProfileResponse userProfileResponse, MultiplePaymentRequest paymentRequest, BigDecimal fee, PaymentResponse paymentResponse, String userName, String transactionId) throws ThirdPartyIntegrationException {

        PaymentTransactionDetail paymentTransactionDetail = new PaymentTransactionDetail();
        paymentTransactionDetail.setAmount(paymentRequest.getAmount());
        paymentTransactionDetail.setFee(fee);
        paymentTransactionDetail.setBiller(paymentRequest.getBillerId());
        paymentTransactionDetail.setCategory(paymentRequest.getCategoryId());
        paymentTransactionDetail.setPaymentRequest(CommonUtils.objectToJson(paymentRequest).orElse(""));
        paymentTransactionDetail.setPaymentResponse(CommonUtils.objectToJson(paymentResponse).orElse(""));
        paymentTransactionDetail.setSuccessful(true);
        paymentTransactionDetail.setThirdPartyName(categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE)));
        paymentTransactionDetail.setTransactionId(transactionId);
        paymentTransactionDetail.setUserAccountNumber(paymentRequest.getSourceWalletAccountNumber());
        paymentTransactionDetail.setUsername(userName);
        paymentTransactionDetail.setReferralCode(userProfileResponse.getReferral());
        paymentTransactionDetail.setPhoneNumber(userProfileResponse.getPhoneNumber());
        paymentTransactionDetail.setEmail(userProfileResponse.getEmail());
        return paymentTransactionRepo.save(paymentTransactionDetail);
    }

    public PaymentTransactionDetail saveFailedTransactionDetail(UserProfileResponse userProfileResponse, PaymentRequest paymentRequest, BigDecimal fee, PaymentResponse paymentResponse, String userName, String transactionId) throws ThirdPartyIntegrationException {
        PaymentTransactionDetail paymentTransactionDetail = new PaymentTransactionDetail();
        paymentTransactionDetail.setAmount(paymentRequest.getAmount());
        paymentTransactionDetail.setFee(fee);
        paymentTransactionDetail.setBiller(paymentRequest.getBillerId());
        paymentTransactionDetail.setCategory(paymentRequest.getCategoryId());
        paymentTransactionDetail.setPaymentRequest(CommonUtils.objectToJson(paymentRequest).orElse(""));
        paymentTransactionDetail.setPaymentResponse(CommonUtils.objectToJson(paymentResponse).orElse(""));
        paymentTransactionDetail.setSuccessful(false);
        paymentTransactionDetail.setThirdPartyName(categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE)));
        paymentTransactionDetail.setTransactionId(transactionId);
        paymentTransactionDetail.setUserAccountNumber(paymentRequest.getSourceWalletAccountNumber());
        paymentTransactionDetail.setUsername(userName);
        paymentTransactionDetail.setReferralCode(userProfileResponse.getReferral());
        return paymentTransactionRepo.save(paymentTransactionDetail);
    }


    public void trackTransactionCount(UserProfileResponse userProfileResponse, String token) throws ThirdPartyIntegrationException {
        TransactionTracker transactionTracker = new TransactionTracker();
        ReferralCodePojo referralCodePojo = getReferralDetails(userProfileResponse.getReferral(), token);
        log.info("referralCodePojo " + referralCodePojo);
        if (referralCodePojo != null) {
            log.info("going here now" + referralCodePojo);
            // this is for users who have been referred by another user
            saveCounter(referralCodePojo,userProfileResponse);
            log.info(" back from here");
        }else{
            log.info("referralCodePojo is null ==" + referralCodePojo);
            // this is for users with no referralCode
            saveCounter(referralCodePojo,userProfileResponse);
        }
    }

    private void saveCounter(ReferralCodePojo referralCodePojo, UserProfileResponse userProfileResponse){
        String referreeId = userProfileResponse.getUserId();
        TransactionTracker transactionOpt = transactionTrackerRepository.findByReferreeId(referreeId);
        log.info("transactionOpt is present :: " + transactionOpt);
        log.info("transactionOpt is present :: referralCodePojo" + referralCodePojo);

        if (transactionOpt !=null && referralCodePojo ==null) {
            // DONT COUNT TRANSACTIONS; USER HAS NO REFERRAL
            log.info("DONT COUNT TRANSACTIONS; USER HAS NO REFERRAL");

        } else if (transactionOpt !=null && referralCodePojo !=null){
            // COUNT TRANSACTIONS
            int newCount = transactionOpt.getCount() + 1;
            transactionOpt.setCount(newCount);
            transactionOpt.setReferralCode(userProfileResponse.getReferral());
            transactionOpt.setReferralCodeOwner(referralCodePojo.getUserId()); // if this is null the obj wont save referralCodePojo.getUserId()
            transactionOpt.setReferreeId(userProfileResponse.getUserId());
            transactionOpt.setTransactionType(TransactionType.BILLS_PAYMENT);
            transactionTrackerRepository.save(transactionOpt);
            log.info("Billspayment counter {} for referralCodePojo !=null :: " + userProfileResponse.getUserId() + "==****==" + newCount);
        } else if (transactionOpt ==null && referralCodePojo !=null){
            // COUNT TRANSACTIONS
            log.info("This  referralCodePojo " + referralCodePojo);
            log.info("This  transactionOpt " + transactionOpt);
            TransactionTracker transactionTracker = new TransactionTracker();
            transactionTracker.setCount(1);
            transactionTracker.setReferralCode(userProfileResponse.getReferral());
            transactionTracker.setReferralCodeOwner(referralCodePojo.getUserId());
            transactionTracker.setReferreeId(userProfileResponse.getUserId());
            transactionTracker.setTransactionType(TransactionType.BILLS_PAYMENT);
            transactionTrackerRepository.save(transactionTracker);
            log.info("This is my first time here Last " + transactionTracker);


        } else if (transactionOpt ==null && referralCodePojo ==null){
            // DONT COUNT TRANSACTIONS; USER HAS NO REFERRAL
            log.info("DONT COUNT TRANSACTIONS; USER HAS NO REFERRAL");
        } else {
            // Create NEW
            log.info("Create NEW");
            TransactionTracker transactionTracker = new TransactionTracker();
            transactionTracker.setCount(1);
            transactionTracker.setReferralCode(userProfileResponse.getReferral());
            transactionTracker.setReferralCodeOwner("");
            transactionTracker.setReferreeId(userProfileResponse.getUserId());
            transactionTracker.setTransactionType(TransactionType.BILLS_PAYMENT);
            transactionTrackerRepository.save(transactionTracker);
            log.info("This is my first time here Last " + transactionTracker);
        }
    }

    public ReferralCodePojo getReferralDetails(String referralCode, String token) throws ThirdPartyIntegrationException {
        log.info("ReferralCode :: " + referralCode);
        try{
            ResponseEntity<ApiResponseBody<ReferralCodePojo>> responseEntity = referralFeignClient.getUserByReferralCode(referralCode,token);
            ApiResponseBody<ReferralCodePojo> responseBody = responseEntity.getBody();
            ReferralCodePojo referralCodePojo = responseBody.getData();
            log.info("referralCodePojo ::: " + referralCodePojo);

            return referralCodePojo;
        } catch (FeignException exception) {
            return null;
            //log.error("FeignException => {}", exception.getCause());
            //throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getErrorMessage(exception.contentUTF8()));
        }
    }

    public TransactionTracker testTracker(TransactionTracker transactionTracker2){
        TransactionTracker transactionTracker = new TransactionTracker();
        transactionTracker.setCount(1);
        transactionTracker.setReferralCode(transactionTracker2.getReferralCode());
        transactionTracker.setReferralCodeOwner(transactionTracker2.getReferralCodeOwner());
        transactionTracker.setReferreeId(transactionTracker2.getReferreeId());
        transactionTracker.setTransactionType(TransactionType.BILLS_PAYMENT);
        return transactionTrackerRepository.save(transactionTracker);
    }

    public List<TransactionTracker> getListOfTransactions(String referralCode) throws ThirdPartyIntegrationException {
        try{
            List<TransactionTracker> transactionTrackerList = transactionTrackerRepository.findByReferralCode(referralCode);
            return transactionTrackerList;
        } catch (Exception  e) {
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }
    public TransactionTracker updateTransactionsStatus(Long id) throws ThirdPartyIntegrationException {
        try{
            Optional<TransactionTracker> transactionTracker = transactionTrackerRepository.findById(id);
            if (transactionTracker.isPresent()){
                transactionTracker.get().setPaid(true);
                return transactionTrackerRepository.save(transactionTracker.get());
            }

        } catch (Exception  e) {
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
        return null;
    }


    public String getErrorMessage(String errorInJson) {
        try {
            log.info("JsonObject => {}", errorInJson);
            return CommonUtils.getObjectMapper().readValue(errorInJson, FundTransferResponse.class).getMessage();
        } catch (JsonProcessingException e) {
            log.error("[JsonProcessingException] : Unable to ", e);
            return Constants.ERROR_MESSAGE;
        }
    }


    // working and tested 30/07/2021
    public List<NewWalletResponse> getWayaOfficialWallet(String token) throws ThirdPartyIntegrationException {

        try {
            ResponseEntity<InfoResponseList> response =  walletFeignClient.getWayaOfficialWallet(token);

            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }
            InfoResponseList infoResponse = response.getBody();
            log.info("mainWalletResponse :: {} " +infoResponse.data);
            List<NewWalletResponse> mainWalletResponse = infoResponse.data;

            return mainWalletResponse;
        } catch (RestClientException  | ThirdPartyIntegrationException  e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

    }


    public List<WalletTransactionPojo> refundFailedTransaction(TransferFromOfficialToMainWallet transfer, String token) throws ThirdPartyIntegrationException {
        try {
            Optional<PaymentTransactionDetail> transactionDetail = paymentTransactionRepo.findByTransId(Long.parseLong(transfer.getBillsPaymentTransactionId()));

            transfer.setTransactionCategory("TRANSFER");
            if (transactionDetail.isPresent()){
                ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> responseEntity =  walletFeignClient.refundFailedTransaction(transfer,token);

                ApiResponseBody<List<WalletTransactionPojo>> infoResponse = responseEntity.getBody();
                List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
                log.info("responseList " + mainWalletResponseList);

                if (responseEntity.getStatusCode().is2xxSuccessful()){
                    saveBillsPaymentRefund(transfer, mainWalletResponseList);
                    transactionDetail.get().setResolved(true);
                    paymentTransactionRepo.save(transactionDetail.get());
                }

                return mainWalletResponseList;
            }
        } catch (RestClientException e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
        return null;
    }



    public void saveBillsPaymentRefund(TransferFromOfficialToMainWallet transfer,List<WalletTransactionPojo> mainWalletResponseList) throws ThirdPartyIntegrationException {
        try{
            BillsPaymentRefund billsPaymentRefund = new BillsPaymentRefund();
            billsPaymentRefund.setAmount(transfer.getAmount());
            billsPaymentRefund.setUserId(transfer.getUserId());
            billsPaymentRefund.setTransactionId(transfer.getBillsPaymentTransactionId());
            billsPaymentRefund.setSuccessful(true);
            billsPaymentRefund.setJsonRequest(CommonUtils.objectToJson(transfer).orElse(""));
            billsPaymentRefund.setJsonResponse(CommonUtils.objectToJson(mainWalletResponseList).orElse(""));

            billsPaymentRefundRepository.save(billsPaymentRefund);
        } catch (Exception e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }




    public void testPushWayaGramPayment(TransferPojo transferPojo) throws JsonProcessingException {
        log.info("data " + transferPojo);
        kafkaTemplate.send("wayagram-payment", CommonUtils.getObjectMapper().writeValueAsString(transferPojo));
        log.info("data after" + transferPojo);
    }

    public void testPushWayaGramProductPayment(TransferPojo transferPojo) throws JsonProcessingException {
        log.info("data " + transferPojo);
        kafkaTemplate.send("wayagram-payment", CommonUtils.getObjectMapper().writeValueAsString(transferPojo));
        log.info("data after" + transferPojo);
    }

}
