package com.wayapay.thirdpartyintegrationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationService {

    private final PaymentTransactionRepo paymentTransactionRepo;
    private final WalletFeignClient walletFeignClient;
    private final CategoryService categoryService;
    @AuditPaymentOperation(stage = Stage.SECURE_FUND, status = Status.START)
    public boolean secureFund(BigDecimal amount, BigDecimal fee, String userName, String userAccountNumber, String transactionId, FeeBearer feeBearer, String token) throws ThirdPartyIntegrationException {

        //Get user default wallet
        NewWalletResponse defaultWallet = getNewDefaultWallet(userName, token);

        if (defaultWallet.getClr_bal_amt().doubleValue() < amount.doubleValue())
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.INSUFFICIENT_FUND);

        //consume
        TransferFromWalletPojo trans = new TransferFromWalletPojo();
        trans.setAmount(FeeBearer.CONSUMER.equals(feeBearer) ? amount.add(fee) : amount);
        trans.setCustomerAccountNumber(defaultWallet.getAccountNo());
        trans.setEventId(EventCharges.AITCOL.name());
        trans.setPaymentReference(transactionId);
        trans.setTranCrncy("NGN");
        trans.setTranNarration(TransactionType.BILLS_PAYMENT.name());
        try {
            boolean check = walletTransfer(trans,token);
            return true;
        } catch (FeignException exception) {
            log.error("FeignException => {}", exception.getCause());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getErrorMessage(exception.contentUTF8()));
        }
    }

//    @AuditPaymentOperation(stage = Stage.SECURE_FUND, status = Status.START)
//    public boolean secureFund(BigDecimal amount, BigDecimal fee, String userName, String userAccountNumber, String transactionId, FeeBearer feeBearer, String token) throws ThirdPartyIntegrationException {
//
//    	//Get user default wallet
//    	MainWalletResponse defaultWallet = walletFeignClient.getDefaultWallet(token);
//        //consume
//    	TransferFromWalletPojo trans = new TransferFromWalletPojo();
//    	trans.setAmount(FeeBearer.CONSUMER.equals(feeBearer) ? amount.add(fee) : amount);
//    	trans.setCustomerWalletId(defaultWallet.getId());
//    	trans.setPaymentReference("BILLS-PAYMENT-TRANSACTION");
//        try {
//            walletFeignClient.transferToUser(trans,token);
//            return true;
//        } catch (FeignException exception) {
//            log.error("FeignException => {}", exception.getCause());
//            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getErrorMessage(exception.contentUTF8()));
//        }
//    }

    @AuditPaymentOperation(stage = Stage.SAVE_TRANSACTION_DETAIL, status = Status.END)
    public PaymentTransactionDetail saveTransactionDetail(PaymentRequest paymentRequest, BigDecimal fee, PaymentResponse paymentResponse, String userName, String transactionId) throws ThirdPartyIntegrationException {
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
        return paymentTransactionRepo.save(paymentTransactionDetail);
    }

    public String getErrorMessage(String errorInJson){
        try {
            log.info("JsonObject => {}", errorInJson);
            return CommonUtils.getObjectMapper().readValue(errorInJson, FundTransferResponse.class).getMessage();
        } catch (JsonProcessingException e) {
            log.error("[JsonProcessingException] : Unable to ", e);
            return Constants.ERROR_MESSAGE;
        }
    }


//    public MainWalletResponse getDefaultWallet(String token) throws ThirdPartyIntegrationException {
//
//        String user;
//
//        ResponseEntity<String> response = null;
//        try {
//            response =  walletFeignClient.getDefaultWalletEntity(token);
//            if (response.getStatusCode().isError()) {
//                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
//            }
//            user = response.getBody();
//            JSONObject jsonpObject = new JSONObject(user);
//            String json = jsonpObject.getJSONObject("data").toString();
//            MainWalletResponse mainWalletResponse = GsonUtils.cast(json, MainWalletResponse.class);
//            return mainWalletResponse;
//
//        } catch (RestClientException | JsonSyntaxException | ThirdPartyIntegrationException | JSONException e) {
//            System.out.println("Error is here " + e.getMessage());
//            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
//        }
//    }

    public boolean walletTransfer(TransferFromWalletPojo transfer, String token) throws ThirdPartyIntegrationException {


//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Accept", "application/json");
//        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString());
//        headers.add("Authorization", "Bearer " + token);
//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity<String> entity = new HttpEntity<String>("", headers);
        String user;
        List<TransactionRequest> langList = new ArrayList<>();
        try {
            ResponseEntity<String> response =  walletFeignClient.transferFromUserToWaya(transfer,token);
            //restTemplate.exchange( builder.toUriString(), HttpMethod.GET, entity, java.lang.String.class,uriParam);
            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }
            user = response.getBody();
            System.out.println(" user " + user);
//            final ObjectMapper objectMapper = new ObjectMapper();
//            TransactionRequest[] langs = objectMapper.readValue(user, TransactionRequest[].class);
//
//            langList = new ArrayList<>(Arrays.asList(langs));
//            System.out.println(" langList " + langList);
//            langList.forEach(x -> System.out.println(x.toString()));
            return true;
        } catch (RestClientException | JsonSyntaxException | ThirdPartyIntegrationException  e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }

    }

//###############################################################################################

    public TransactionRequest debitDefaultWallet(TransferFromWalletPojo transfer, String token) throws ThirdPartyIntegrationException {

        String user;

        ResponseEntity<String> response = null;
        try {
            response =  walletFeignClient.transferFromUserToWaya(transfer,token);
            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }
            user = response.getBody();
            JSONObject jsonpObject = new JSONObject(user);
            String json = jsonpObject.getJSONObject("data").toString();
            TransactionRequest mainWalletResponse = GsonUtils.cast(json, TransactionRequest.class);
            return mainWalletResponse;

        } catch (RestClientException | JsonSyntaxException | ThirdPartyIntegrationException | JSONException e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }


    public NewWalletResponse getNewDefaultWallet(String userId, String token) throws ThirdPartyIntegrationException {

        log.info("HERE " + userId);
        log.info("token " + token);
        String user;

        ResponseEntity<String> response = null;
        try {
            response =  walletFeignClient.getDefaultWallet(userId, token);

            System.out.println("inside getNewDefaultWallet " + response);

            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }
            user = response.getBody();
            JSONObject jsonpObject = new JSONObject(user);
            String json = jsonpObject.getJSONObject("data").toString();
            System.out.println("inside json " + json);
            NewWalletResponse mainWalletResponse = GsonUtils.cast(json, NewWalletResponse.class);
            System.out.println("inside json  mainWalletResponse " + mainWalletResponse);
            return mainWalletResponse;

        } catch (RestClientException | JsonSyntaxException | ThirdPartyIntegrationException | JSONException e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        }
    }


    // yet to be implemented
    public List<NewWalletResponse> getWayaCommissionWallet(String token) throws ThirdPartyIntegrationException {

//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Accept", "application/json");
//        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString());
//        headers.add("Authorization", "Bearer " + token);
//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity<String> entity = new HttpEntity<String>("", headers);
        String user;
        List<NewWalletResponse> langList = new ArrayList<>();
        try {
            ResponseEntity<String> response =  walletFeignClient.getWayaCommissionWallet(token);
            //restTemplate.exchange( builder.toUriString(), HttpMethod.GET, entity, java.lang.String.class,uriParam);
            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }
            user = response.getBody();
            System.out.println(" user " + user);
            final ObjectMapper objectMapper = new ObjectMapper();
            NewWalletResponse[] langs = objectMapper.readValue(user, NewWalletResponse[].class);

            langList = new ArrayList<>(Arrays.asList(langs));
            System.out.println(" langList " + langList);
            langList.forEach(x -> System.out.println(x.toString()));

        } catch (RestClientException | JsonSyntaxException | ThirdPartyIntegrationException  e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return langList;
    }


    // working and tested 30/07/2021
    public List<NewWalletResponse> getWayaOfficialWallet(String token) throws ThirdPartyIntegrationException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString());
        headers.add("Authorization", "Bearer " + token);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<String>("", headers);
        String user;
        List<NewWalletResponse> langList = new ArrayList<>();

        try {
            ResponseEntity<String> response =  walletFeignClient.getWayaOfficialWallet(token);
            // ResponseEntity<String> response = restTemplate.exchange( builder.toUriString(), HttpMethod.GET, entity, java.lang.String.class);

            if (response.getStatusCode().isError()) {
                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
            }
            user = response.getBody();
            JSONObject obj = new JSONObject(user);
            JSONArray scores = obj.getJSONArray("data");
            for (int i = 0; i < scores.length(); i++) {
                JSONObject element = scores.getJSONObject(i);

                NewWalletResponse object = new NewWalletResponse();
                object.setId(element.getLong("id"));
                object.setDelFlg(element.getBoolean("del_flg"));
                object.setEntity_cre_flg(element.getBoolean("entity_cre_flg"));
                object.setSol_id(element.getString("sol_id"));
                object.setBacid(element.getString("bacid"));
                object.setAccountNo(element.getString("accountNo"));
                object.setAcct_name(element.getString("acct_name"));
                object.setGl_code(element.getString("gl_code"));
                object.setProduct_code(element.getString("product_code"));
                object.setAcct_ownership(element.getString("acct_ownership"));
                object.setFrez_code(element.getString("frez_code"));
                object.setFrez_reason_code(element.getString("frez_reason_code"));
                object.setAcct_opn_date(element.getString("acct_opn_date"));
                object.setAcct_cls_flg(element.getString("acct_cls_flg"));
                object.setClr_bal_amt(element.getDouble("clr_bal_amt"));
                object.setUn_clr_bal_amt(element.getDouble("un_clr_bal_amt"));
                object.setHashed_no(element.getString("hashed_no"));
                object.setInt_paid_flg(element.getBoolean("int_paid_flg"));
                object.setInt_coll_flg(element.getBoolean("int_coll_flg"));
                object.setLchg_user_id(element.getString("lchg_user_id"));
                object.setLchg_time(element.getString("lchg_time"));
                object.setRcre_user_id(element.getString("rcre_user_id"));
                object.setRcre_time(element.getString("rcre_time"));
                object.setAcct_crncy_code(element.getString("acct_crncy_code"));
                object.setLien_amt(element.getDouble("lien_amt"));
                object.setProduct_type(element.getString("product_type"));
                object.setCum_dr_amt(element.getDouble("cum_dr_amt"));
                object.setCum_cr_amt(element.getDouble("cum_cr_amt"));
                object.setChq_alwd_flg(element.getBoolean("chq_alwd_flg"));
                object.setCash_dr_limit(element.getDouble("cash_dr_limit"));
                object.setXfer_dr_limit(element.getDouble("xfer_dr_limit"));
                object.setCash_cr_limit(element.getDouble("cash_cr_limit"));
                object.setXfer_cr_limit(element.getDouble("xfer_cr_limit"));
                object.setAcct_cls_date(element.getString("acct_cls_date"));
                object.setLast_tran_date(element.getString("last_tran_date"));
                object.setLast_tran_id_dr(element.getString("last_tran_id_dr"));
                object.setLast_tran_id_cr(element.getString("last_tran_id_cr"));
                object.setWalletDefault(element.getBoolean("walletDefault"));

                langList.add(object);
            }


        } catch (RestClientException | JsonSyntaxException | ThirdPartyIntegrationException  e) {
            System.out.println("Error is here " + e.getMessage());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return langList;
    }


}
