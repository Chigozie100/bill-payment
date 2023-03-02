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
import com.wayapay.thirdpartyintegrationservice.service.profile.ProfileFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;
import com.wayapay.thirdpartyintegrationservice.service.wallet.FundTransferResponse;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationService {

    private final PaymentTransactionRepo paymentTransactionRepo;
    private final WalletFeignClient walletFeignClient;
    private final CategoryService categoryService;
    private final ProfileFeignClient profileFeignClient;
    private final LogFeignClient logFeignClient;
    private final BillsPaymentRefundRepository billsPaymentRefundRepository;
    private final TransactionTrackerRepository transactionTrackerRepository;

    public UserProfileResponse getUserProfile(String userName, String token) throws ThirdPartyIntegrationException {
        UserProfileResponse userProfileResponse;
        try {
            ResponseEntity<ProfileResponseObject> responseEntity = profileFeignClient.getUserProfile(userName, token);
            ProfileResponseObject infoResponse =  responseEntity.getBody();
            assert infoResponse != null;
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

            ResponseObj<LogRequest> infoResponse = response.getBody();
            LogRequest logRequest1 = Objects.requireNonNull(infoResponse).data;
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
        LogRequest logs = creatLog(logRequest, token);
        log.info("done logging...." + logs);

    }

    private NewWalletResponse getUserWallet(String userAccountNumber, String token, Boolean isAdmin){
        // if(isAdmin){
        //     ResponseEntity<InfoResponse> responseEntity = walletFeignClient.getUserWallet(userAccountNumber, systemToken);
        //     InfoResponse infoResponse = responseEntity.getBody();
        //     return Objects.requireNonNull(infoResponse).data;
        // }else{
            ResponseEntity<InfoResponse> responseEntity = walletFeignClient.getUserWalletByUser(userAccountNumber, token);
            InfoResponse infoResponse = responseEntity.getBody();
            return Objects.requireNonNull(infoResponse).data;
        // }

    }

    private void checkAccountBalance(NewWalletResponse mainWalletResponse, BigDecimal amount) throws ThirdPartyIntegrationException {
       if (mainWalletResponse.getClr_bal_amt() < amount.doubleValue())
        throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.INSUFFICIENT_FUND);
    }

    @AuditPaymentOperation(stage = Stage.SECURE_FUND, status = Status.START)
    public boolean secureFundAdmin(BigDecimal amount, BigDecimal fee, String userName, String userAccountNumber, String transactionId, FeeBearer feeBearer, String token, String pin, String billType, String eventId) throws ThirdPartyIntegrationException {
        //Get user default wallet
        processPayment( amount,  fee,  userName,  userAccountNumber,  transactionId,  feeBearer,  token, pin, billType, eventId, true);
        return true;
    }


    private void processPayment(BigDecimal amount, BigDecimal fee, String userName, String userAccountNumber, String transactionId, FeeBearer feeBearer, String token,  String pin, String billType, String eventId, Boolean isAdmin) throws ThirdPartyIntegrationException {
        log.info(" inside processPayment ::  " + eventId);
        
        NewWalletResponse mainWalletResponse2 = getUserWallet(userAccountNumber, token, isAdmin);

        checkAccountBalance(mainWalletResponse2,amount);

        //consume
        TransferFromWalletPojo trans = new TransferFromWalletPojo();
        trans.setAmount(FeeBearer.CONSUMER.equals(feeBearer) ? amount.add(fee) : amount);

        trans.setCustomerAccountNumber(mainWalletResponse2.getAccountNo());
        trans.setEventId(eventId);
        trans.setPaymentReference(transactionId);
        trans.setTranCrncy("NGN");
        trans.setTransactionCategory(billType);
        trans.setTranNarration(TransactionType.BILLS_PAYMENT.name());
        trans.setUserId(Long.parseLong(userName));
        try {
            walletFeignClient.transferFromUserToWaya(trans,token, pin);

        } catch (FeignException exception) {
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getErrorMessage(exception.contentUTF8()));
        }
    }


    @AuditPaymentOperation(stage = Stage.SECURE_FUND, status = Status.START)
    public boolean secureFund(BigDecimal amount, BigDecimal fee, String userName, String userAccountNumber, String transactionId, FeeBearer feeBearer, String token, String pin, String billType, String eventId) throws ThirdPartyIntegrationException {
        //Get user default wallet
        processPayment( amount,  fee,  userName,  userAccountNumber,  transactionId,  feeBearer,  token, pin,  billType, eventId, false);
        return true;
    }

    @AuditPaymentOperation(stage = Stage.SAVE_TRANSACTION_DETAIL, status = Status.END)
    public PaymentTransactionDetail saveTransactionDetail(UserProfileResponse userProfileResponse, PaymentRequest paymentRequest, BigDecimal fee, PaymentResponse paymentResponse, String userName, String transactionId) throws ThirdPartyIntegrationException {

        return processTransactionStatus(true,  userProfileResponse,  paymentRequest,  fee,  paymentResponse,  userName,  transactionId);
    }

    private PaymentTransactionDetail processTransactionStatus(boolean isSuccessful, UserProfileResponse userProfileResponse, PaymentRequest paymentRequest, BigDecimal fee, PaymentResponse paymentResponse, String userName, String transactionId) throws ThirdPartyIntegrationException {
        PaymentTransactionDetail paymentTransactionDetail = new PaymentTransactionDetail();
        paymentTransactionDetail.setAmount(paymentRequest.getAmount());
        paymentTransactionDetail.setFee(fee);
        paymentTransactionDetail.setBiller(paymentRequest.getBillerId());
        paymentTransactionDetail.setCategory(paymentRequest.getCategoryId());
        paymentTransactionDetail.setPaymentRequest(CommonUtils.objectToJson(paymentRequest).orElse(""));
        paymentTransactionDetail.setPaymentResponse(CommonUtils.objectToJson(paymentResponse).orElse(""));
        paymentTransactionDetail.setSuccessful(isSuccessful);
        paymentTransactionDetail.setThirdPartyName(categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE)));
        paymentTransactionDetail.setTransactionId(transactionId);
        paymentTransactionDetail.setUserAccountNumber(paymentRequest.getSourceWalletAccountNumber());
        paymentTransactionDetail.setUsername(userName);
        paymentTransactionDetail.setReferralCode(userProfileResponse.getReferral());
        return paymentTransactionRepo.save(paymentTransactionDetail);
    }


    public void saveFailedTransactionDetail(UserProfileResponse userProfileResponse, PaymentRequest paymentRequest, BigDecimal fee, PaymentResponse paymentResponse, String userName, String transactionId) throws ThirdPartyIntegrationException {
       processTransactionStatus(false,  userProfileResponse,  paymentRequest,  fee,  paymentResponse,  userName,  transactionId);
    }

    public List<TransactionTracker> getListOfTransactions(String referralCode) throws ThirdPartyIntegrationException {
        try{
            return transactionTrackerRepository.findByReferralCode(referralCode);
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
            return Objects.requireNonNull(infoResponse).data;
        } catch (RestClientException  | ThirdPartyIntegrationException  e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

    }

    public List<WalletTransactionPojo> refundFailedTransaction(TransferFromOfficialToMainWallet transfer, String token,String pin) throws ThirdPartyIntegrationException {
        try {
            Optional<PaymentTransactionDetail> transactionDetail = paymentTransactionRepo.findByTransactionId2(transfer.getBillsPaymentTransactionId());
            if (!transactionDetail.isPresent()){
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
            }
            if(transactionDetail.get().isResolved()){
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ALREADY_RESOLVED);
            }
            transfer.setTransactionCategory("TRANSFER");

                ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> responseEntity =  walletFeignClient.refundFailedTransaction(transfer, token, pin);

                ApiResponseBody<List<WalletTransactionPojo>> infoResponse = responseEntity.getBody();
                List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
                log.info("responseList " + mainWalletResponseList);

                if (responseEntity.getStatusCode().is2xxSuccessful()){
                    saveBillsPaymentRefund(transfer, mainWalletResponseList);
                    transactionDetail.get().setResolved(true);
                    paymentTransactionRepo.save(transactionDetail.get());
                }

                return mainWalletResponseList;

        } catch (RestClientException e) {
            log.info("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
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
            log.info("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }

}
