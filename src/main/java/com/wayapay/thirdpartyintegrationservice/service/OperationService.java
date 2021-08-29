package com.wayapay.thirdpartyintegrationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseObj;
import com.wayapay.thirdpartyintegrationservice.service.notification.*;
import com.wayapay.thirdpartyintegrationservice.service.profile.ProfileFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;
import com.wayapay.thirdpartyintegrationservice.service.wallet.FundTransferResponse;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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


    public UserProfileResponse getUserProfile(String userName, String token) throws ThirdPartyIntegrationException {
        UserProfileResponse userProfileResponse = null;
      try {
            ResponseEntity<ProfileResponseObject> responseEntity = profileFeignClient.getUserProfile(userName, token);
            ProfileResponseObject infoResponse = (ProfileResponseObject) responseEntity.getBody();
            userProfileResponse = infoResponse.data;
            log.info("userProfileResponse :: " +userProfileResponse);
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
      return userProfileResponse;
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

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Boolean>  smsNotification(SmsEvent smsEvent, String token) throws ThirdPartyIntegrationException {
        ResponseEntity<ResponseObj>  responseEntity = null;

        String checkSMSGateway = getActiveSMSGateway(token).getName();
        try {

        switch(checkSMSGateway) {
            case "ATALKING":
                responseEntity = notificationFeignClient.smsNotifyUserAtalking(smsEvent,token);
                break;
            case "INFOBIP":
                responseEntity = notificationFeignClient.smsNotifyUserInfobip(smsEvent,token);
                break;
            case "TWILIO":
                responseEntity = notificationFeignClient.smsNotifyUserTwilio(smsEvent,token);
                break;
            default:
                responseEntity = notificationFeignClient.smsNotifyUserTwilio(smsEvent,token);
        }

            ResponseObj infoResponse = (ResponseObj) responseEntity.getBody();
            log.info("userProfileResponse sms sent status :: " +infoResponse.status);
            return CompletableFuture.completedFuture(infoResponse.status);
        } catch (Exception e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

    }


    public SMSChargeResponse getSMSCharges(String token) throws ThirdPartyIntegrationException {
        log.info("inside :: getSMSCharges " + token);
        try {
            ResponseEntity<ResponseObj<SMSChargeResponse>>  responseEntity = notificationFeignClient.getActiveSMSCharge(token);
            ResponseObj infoResponse = (ResponseObj) responseEntity.getBody();
            log.info("getSMSCharges  :: " +responseEntity.getBody());
            SMSChargeResponse smsChargeResponse = (SMSChargeResponse) infoResponse.data;
            return smsChargeResponse;

        } catch (RestClientException e) {
            log.info(e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

    }




    @AuditPaymentOperation(stage = Stage.SECURE_FUND, status = Status.START)
    public boolean secureFund(BigDecimal amount, BigDecimal fee, String userName, String userAccountNumber, String transactionId, FeeBearer feeBearer, String token) throws ThirdPartyIntegrationException {
        //Get user default wallet

        ResponseEntity<InfoResponse> responseEntity = walletFeignClient.getDefaultWallet(userName, token);
        InfoResponse infoResponse = (InfoResponse) responseEntity.getBody();
        log.info("mainWalletResponse :: {} " +infoResponse.data);
        NewWalletResponse mainWalletResponse = infoResponse.data;

        if (mainWalletResponse.getClr_bal_amt().doubleValue() < amount.doubleValue())
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.INSUFFICIENT_FUND);

        //consume
        TransferFromWalletPojo trans = new TransferFromWalletPojo();
        trans.setAmount(FeeBearer.CONSUMER.equals(feeBearer) ? amount.add(fee) : amount);

        trans.setCustomerAccountNumber(mainWalletResponse.getAccountNo());
        trans.setEventId(EventCharges.AITCOL.name());
        trans.setPaymentReference(transactionId);
        trans.setTranCrncy("NGN");
        trans.setTranNarration(TransactionType.BILLS_PAYMENT.name());
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
                InfoResponseList infoResponse = (InfoResponseList) response.getBody();
                log.info("mainWalletResponse :: {} " +infoResponse.data);
                List<NewWalletResponse> mainWalletResponse = infoResponse.data;

                return mainWalletResponse;
            } catch (RestClientException  | ThirdPartyIntegrationException  e) {
                System.out.println("Error is here " + e.getMessage());
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
            }

        }


    }

