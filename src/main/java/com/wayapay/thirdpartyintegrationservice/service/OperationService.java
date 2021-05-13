package com.wayapay.thirdpartyintegrationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.dto.MainWalletResponse;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequest;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentResponse;
import com.wayapay.thirdpartyintegrationservice.dto.TransactionRequest;
import com.wayapay.thirdpartyintegrationservice.dto.TransferFromWalletPojo;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.service.wallet.FundTransferRequest;
import com.wayapay.thirdpartyintegrationservice.service.wallet.FundTransferResponse;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URISyntaxException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationService {

    private final PaymentTransactionRepo paymentTransactionRepo;
    private final WalletFeignClient walletFeignClient;
    private final CategoryService categoryService;

    @AuditPaymentOperation(stage = Stage.SECURE_FUND, status = Status.START)
    public boolean secureFund(BigDecimal amount, BigDecimal fee, String userName, String userAccountNumber, String transactionId, FeeBearer feeBearer, String token) throws ThirdPartyIntegrationException, URISyntaxException {

    	//Get user default wallet
    	MainWalletResponse defaultWallet = walletFeignClient.getDefaultWallet(token);
        //consume
    	TransferFromWalletPojo trans = new TransferFromWalletPojo();
    	trans.setAmount(amount.add(fee).doubleValue());
    	trans.setCustomerWalletId(defaultWallet.getId());
    	trans.setPaymentReference("BILLS-PAYMENT-TRANSACTION");
        FundTransferRequest fundTransferRequest = new FundTransferRequest();
        fundTransferRequest.setAmount(FeeBearer.CONSUMER.equals(feeBearer) ? amount.add(fee) : amount);
        fundTransferRequest.setId(0L);
        fundTransferRequest.setAccountNo(userAccountNumber);
        fundTransferRequest.setDescription("Billspayment -> "+transactionId);
        fundTransferRequest.setTransactionType("DEBIT");
        try {
        	makeTransaction("DEBIT", trans, token);
//            walletFeignClient.doTransaction(fundTransferRequest);
        } catch (FeignException exception) {
            log.error("FeignException => {}", exception.getCause());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getErrorMessage(exception.contentUTF8()));
        }

        return true;
    }
    
    private ResponseEntity<TransactionRequest> makeTransaction(String command,TransferFromWalletPojo transferPojo ,  String token) throws URISyntaxException {
		
    	
		RestTemplate restTemplate = new RestTemplate();
//		final String baseUrl = "http://46.101.41.187:9196/api/v1/wallet-transactions";
		final String baseUrl = "http://157.230.223.54:9009/transaction/new/transfer/to/user";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);  
        headers.setContentType(MediaType.APPLICATION_JSON);
     // request body parameters
     // build the request
        HttpEntity request = new HttpEntity(transferPojo,headers);
        
     // build the request
        
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
        		.queryParam("command", command);
        System.out.println(builder.toUriString());
        ResponseEntity<TransactionRequest> response = restTemplate.exchange(
        		builder.toUriString(),
                HttpMethod.POST,
                request,
                TransactionRequest.class
        );
        return response;
	}


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

}
