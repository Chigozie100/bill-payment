package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.commission.CommissionDto;
import com.wayapay.thirdpartyintegrationservice.service.commission.CommissionFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.commission.MerchantCommissionTrackerDto;
import com.wayapay.thirdpartyintegrationservice.service.commission.UserCommissionDto;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommissionOperationService {
    private final CommissionFeignClient commissionFeignClient;
    private final WalletFeignClient walletFeignClient;
    private final NotificationService notificationService;


    public UserCommissionDto findUserCommission(UserType userType, String token) throws ThirdPartyIntegrationException {

        try {
            ResponseEntity<ApiResponseBody<UserCommissionDto>> response = commissionFeignClient.userCommissionExtra(userType, TransactionType.BILLS_PAYMENT,token);
            ApiResponseBody<UserCommissionDto> infoResponse = response.getBody();
            UserCommissionDto userCommissionDto = infoResponse != null ? infoResponse.getData() : null;

            log.info("Response from Commission Service" + response);
            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }

           return userCommissionDto;

        } catch (RestClientException e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }
//
//    // method to pay
//    public TransactionRequest payUserCommission(UserType userType, String token, String username) throws ThirdPartyIntegrationException {
//        log.info("UserType ::: " + userType);
//        String transactionId = CommonUtils.generatePaymentTransactionId();
//
//        UserCommissionDto userCommissionDto = findUserCommission(userType,token);   // get Commission Value For the Customer Type
//
//        NewWalletResponse wayCommissionResponse = getUserCommissionWallet(username,token); // get user commission
//
//        TransferFromWalletPojo trans = new TransferFromWalletPojo();
//        trans.setAmount(userCommissionDto.getCommissionValue());
//        trans.setCustomerAccountNumber(wayCommissionResponse.getAccountNo()); // customer wallet ID
//        trans.setEventId(EventCharges.COMPAYM.name());
//        trans.setPaymentReference(transactionId);
//        trans.setTranCrncy("NGN");
//        trans.setTranNarration("COMMISSION-PAYMENT-TRANSACTION");
//
//        try{
//            log.info("BillsPayment:Commission {} Sending data to Commission Service loading .....");
//            TransactionRequest transactionRequest =  new TransactionRequest();
//
//            walletService.walletTransfer(trans,token);
//
//            PaymentTransactionDetail paymentTransactionDetail = new PaymentTransactionDetail();
//            paymentTransactionDetail.setAmount(BigDecimal.valueOf(0.0));
//            paymentTransactionDetail.setPaymentRequest(CommonUtils.objectToJson(trans).orElse(""));
//            paymentTransactionDetail.setPaymentResponse(CommonUtils.objectToJson(null).orElse(""));
//
//           // notificationService.pushINAPP(paymentTransactionDetail, token, null, trans.toString());
//
//            log.info("BillsPayment:Commission {} Done sending data .....");    // register under commission history
//            return transactionRequest;
//        }catch (Exception ex){
//            throw new ThirdPartyIntegrationException(HttpStatus.MULTI_STATUS, Constants.ERROR_MESSAGE);
//        }
//    }

    public void payUserCommission(UserType userType, String token, String userId) throws ThirdPartyIntegrationException {
        TransferFromWalletPojo transfer = new TransferFromWalletPojo();

        //String token = BearerTokenUtil.getBearerTokenHeader();
        UserCommissionDto userCommissionDto = findUserCommission(userType,token);
        NewWalletResponse userCommissionWallet = getUserCommissionWallet(userId,token); // get user commission wallet

        transfer.setAmount(userCommissionDto.getCommissionValue());
        transfer.setEventId(EventCharges.COMPAYM.name());
        transfer.setPaymentReference(CommonUtils.generatePaymentTransactionId());
        transfer.setCustomerAccountNumber(userCommissionWallet != null ? userCommissionWallet.getAccountNo() : null);
        transfer.setTranCrncy("NGN");
        transfer.setTranNarration("COMMISSION-PAYMENT-TRANSACTION");

        ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>>  responseEntity = walletFeignClient.officialCommissionToUserCommission(transfer,token);
        ApiResponseBody<List<WalletTransactionPojo>> infoResponse = responseEntity.getBody();

        List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
        List<WalletTransactionPojo> walletTransactionPojoList = new ArrayList<>(Objects.requireNonNull(mainWalletResponseList));

        CompletableFuture.runAsync(() -> {
            CommissionDto commissionDto = new CommissionDto();

            commissionDto.setUserId(userId);
            commissionDto.setCommissionValue(transfer.getAmount());
            commissionDto.setJsonRequest(transfer);
            commissionDto.setJsonResponse(walletTransactionPojoList);
            commissionDto.setTransactionType(TransactionType.TRANSFER);
            commissionDto.setUserType(userType);

            ResponseEntity<ApiResponseBody<CommissionDto>> resp = commissionFeignClient.addCommissionHistory(commissionDto, token);
            ApiResponseBody<CommissionDto> commissionDtoApiResponseBody = resp.getBody();
            log.info(String.format("Bills-payment: payUserCommission response :: {} %s", commissionDtoApiResponseBody != null ? commissionDtoApiResponseBody.getData() : null));
        });

//        Map<String, String> map = new HashMap<>();
//        map.put("request", buildTransJson(transfer).toString());
//        map.put("module", "CommissionWalletController");
//        map.put("action", "CREATE");
//        map.put("message", "FIXED-PAYMENT-FOR-COMMISSION");
//        map.put("response", buildTransJsonResponse(mainWalletResponseList).toString());
//
        List<String> inAppRecipient = new ArrayList<>();
        inAppRecipient.add(userId);

        Map<String, String> dto = new HashMap<>();
        dto.put("userId",transfer.getUserId().toString());
        dto.put("ref", transfer.getPaymentReference());
        dto.put("amount", transfer.getAmount().toString());
        dto.put("sender", "WAYA-ADMIN");
        dto.put("initiator", "system");
        dto.put("in_app_recipient", inAppRecipient.toString());


        if (infoResponse.getStatus()){
            dto.put("message", "Fund Merchant Commission Wallet Successful:: " + transfer.getUserId());
        }else{
            dto.put("message", "Error Funding Merchant Commission Wallet :: " + transfer.getUserId());
        }

        CompletableFuture.runAsync(() -> {
            try {
                notificationService.pushINAPP(dto, token, null,walletTransactionPojoList);
//                notificationService.pushINAPP(dto,token);
            } catch (ThirdPartyIntegrationException e) {
                e.printStackTrace();
            }
        });

//        CompletableFuture.runAsync(() -> {
//            try {
//                UserProfileResponse userProfileResponse = profileService.getUserProfile(transfer.getUserId().toString(),token);
//                notificationService.pushSMS(dto,token, userProfileResponse);
//            } catch (CommissionException e) {
//                e.printStackTrace();
//            }
//        });

    }


    private NewWalletResponse getUserCommissionWallet(String userId, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ApiResponseBody<NewWalletResponse>> commissionWallet = walletFeignClient.getUserCommissionWallet(token,userId);
            ApiResponseBody<NewWalletResponse> commissionWalletBody = commissionWallet.getBody();

            return commissionWalletBody != null ? commissionWalletBody.getData() : null;
        }catch (Exception exception){
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, exception.getMessage());
        }

    }

    public MerchantCommissionTrackerDto saveMerchantCommission(MerchantCommissionTrackerDto request, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ApiResponseBody<MerchantCommissionTrackerDto>> responseEntity = commissionFeignClient.recordMerchantCommission(request,token);
            ApiResponseBody<MerchantCommissionTrackerDto> responseBody = responseEntity.getBody();
            log.info("Billspyament:: {} in here saveMerchantCommission ::: " + responseBody.getData());
            return responseBody != null ? responseBody.getData() : null;
        }catch (Exception exception){
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, exception.getMessage());
        }

    }






//
////    public TransactionRequest fundDefaultWallet(TransferFromWalletPojo walletDto, String token) throws ThirdPartyIntegrationException {
////
////        String user;
////        ResponseEntity<String> response = null;
////        try {
////            response =  walletFeignClient.creditDefaultWallet(walletDto,token);
////
////            if (response.getStatusCode().isError()) {
////                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
////            }
////            user = response.getBody();
////            JSONObject jsonpObject = new JSONObject(user);
////            String json = jsonpObject.getJSONObject("data").toString();
////            TransactionRequest mainWalletResponse = GsonUtils.cast(json, TransactionRequest.class);
////            return mainWalletResponse;
////
////        } catch (RestClientException | JSONException e) {
////            System.out.println("Error in fundDefaultWallet " + e.getMessage());
////            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
////        }
////    }
//
//    public TransactionRequest fundCommissionWallet(TransferFromWalletPojo walletDto, String token) throws ThirdPartyIntegrationException {
//
//        //String url = baseUrl + "/transaction/new/wallet/to/wallet?command=CREDIT";
//        String user;
//        ResponseEntity<String> response = null;
//        try {
//            response =  walletFeignClient.fundCommissionWallet(walletDto,token);
//
//            if (response.getStatusCode().isError()) {
//                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
//            }
//            user = response.getBody();
//            JSONObject jsonpObject = new JSONObject(user);
//            String json = jsonpObject.getJSONObject("data").toString();
//            TransactionRequest mainWalletResponse = GsonUtils.cast(json, TransactionRequest.class);
//            return mainWalletResponse;
//
//        } catch (RestClientException | JSONException e) {
//            System.out.println("Error in fundDefaultWallet " + e.getMessage());
//            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
//        }
//    }
}
