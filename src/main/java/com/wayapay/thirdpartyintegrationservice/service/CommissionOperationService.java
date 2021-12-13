package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.commission.*;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
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

    private BigDecimal computePercentage(BigDecimal amount, BigDecimal percentageValue){
        BigDecimal per = BigDecimal.valueOf(percentageValue.doubleValue() / 100);
        BigDecimal ans = BigDecimal.valueOf(per.doubleValue() * amount.doubleValue());
        return ans;
    }

    public void payUserCommission(UserType userType,String userId, String token, BigDecimal amount) throws ThirdPartyIntegrationException {
        TransferFromWalletPojo transfer = new TransferFromWalletPojo();

        //String token = BearerTokenUtil.getBearerTokenHeader();
        UserCommissionDto userCommissionDto = findUserCommission(userType,token);
        NewWalletResponse userCommissionWallet = getUserCommissionWallet(userId,token); // get user commission wallet

        transfer.setAmount(computePercentage(amount,userCommissionDto.getCommissionValue()));
        transfer.setEventId(EventCharges.COMPAYM.name());
        transfer.setPaymentReference(CommonUtils.generatePaymentTransactionId());
        transfer.setCustomerAccountNumber(userCommissionWallet != null ? userCommissionWallet.getAccountNo() : null);
        transfer.setTranCrncy("NGN");
        transfer.setTranNarration("COMMISSION-PAYMENT-TRANSACTION");

        ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>>  responseEntity = walletFeignClient.officialCommissionToUserCommission(transfer,token);
        ApiResponseBody<List<WalletTransactionPojo>> infoResponse = responseEntity.getBody();

        List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
        List<WalletTransactionPojo> walletTransactionPojoList = new ArrayList<>(Objects.requireNonNull(mainWalletResponseList));

        saveCommissionHistory(userId,transfer, walletTransactionPojoList,userType,token);

        inAppNotification(userId, transfer, walletTransactionPojoList, token, infoResponse);


    }

    public void payOrganisationCommission(UserType userType,String billerId,String userId, String token, BigDecimal amount) throws ThirdPartyIntegrationException {

        TransferFromWalletPojo transfer = new TransferFromWalletPojo();

        //String token = BearerTokenUtil.getBearerTokenHeader();
        OrganisationCommissionResponse orgCommission = getOrgCommission(billerId,token);   // find organisation Commission Details
        NewWalletResponse userCommissionWallet = getUserCommissionWallet(orgCommission.getCorporateUserId(),token); // get user commission wallet

        transfer.setAmount(computePercentage(amount,BigDecimal.valueOf(orgCommission.getCommissionValue())));
        transfer.setEventId(EventCharges.COMPAYM.name());
        transfer.setPaymentReference(CommonUtils.generatePaymentTransactionId());
        transfer.setCustomerAccountNumber(userCommissionWallet != null ? userCommissionWallet.getAccountNo() : null);
        transfer.setTranCrncy("NGN");
        transfer.setTranNarration("MERCHANT-COMMISSION-PAYMENT");

        ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>>  responseEntity = walletFeignClient.officialCommissionToUserCommission(transfer,token);
        ApiResponseBody<List<WalletTransactionPojo>> infoResponse = responseEntity.getBody();

        List<WalletTransactionPojo> mainWalletResponseList = infoResponse != null ? infoResponse.getData() : null;
        List<WalletTransactionPojo> walletTransactionPojoList = new ArrayList<>(Objects.requireNonNull(mainWalletResponseList));

        saveCommissionHistory(userId,transfer, walletTransactionPojoList,userType,token);

        inAppNotification(orgCommission.getCorporateUserId(), transfer, walletTransactionPojoList, token, infoResponse);

        emailNotification(orgCommission, userId, transfer, walletTransactionPojoList, token,infoResponse);

    }

    private NewWalletResponse getUserCommissionWallet(String userId, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ApiResponseBody<NewWalletResponse>> commissionWallet = walletFeignClient.getUserCommissionWallet(userId,token);
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


    private OrganisationCommissionResponse getOrgCommission(String biller, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ApiResponseBody<OrganisationCommissionResponse>> responseEntity = commissionFeignClient.getOrgCommission(biller,token);
            ApiResponseBody<OrganisationCommissionResponse> responseBody = responseEntity.getBody();
            log.info("Billspyament:: {} in here saveMerchantCommission ::: " + responseBody.getData());
            return responseBody != null ? responseBody.getData() : null;
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
            commissionDto.setTransactionType(TransactionType.TRANSFER);
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
        dto.put("in_app_recipient", inAppRecipient.toString());


        if (infoResponse.getStatus()){
            dto.put("message", "Fund Merchant Commission Wallet Successful:: " + transfer.getUserId());
        }else{
            dto.put("message", "Error Funding Merchant Commission Wallet :: " + transfer.getUserId());
        }

        CompletableFuture.runAsync(() -> {
            try {
                notificationService.pushINAPP(dto, token);
            } catch (ThirdPartyIntegrationException e) {
                e.printStackTrace();
            }
        });
    }

    private void emailNotification(OrganisationCommissionResponse response, String userId, TransferFromWalletPojo transfer, List<WalletTransactionPojo> walletTransactionPojoList, String token, ApiResponseBody<List<WalletTransactionPojo>> infoResponse){
        List<String> inAppRecipient = new ArrayList<>();
        inAppRecipient.add(userId);

        Map<String, String> map = new HashMap<>();

        map.put("paymentTransactionAmount", transfer.getAmount().toString());
        map.put("userId", response.getCorporateUserId());
        map.put("email", response.getCorporateUserEmail());
        map.put("phoneNumber", response.getCorporateUserPhoneNumber());
        map.put("surname", response.getCorporateUserId());
        map.put("firstName", response.getMerchantName());
        map.put("middleName", response.getMerchantName());

        if (infoResponse.getStatus()){
            map.put("message", "Fund Merchant Commission Wallet Successful:: " + transfer.getUserId());
        }else{
            map.put("message", "Error Funding Merchant Commission Wallet :: " + transfer.getUserId());
        }

        CompletableFuture.runAsync(() -> {
            try {
                notificationService.pushEMAIL(map, token);
            } catch (ThirdPartyIntegrationException e) {
                e.printStackTrace();
            }
        });
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