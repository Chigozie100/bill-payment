package com.wayapay.thirdpartyintegrationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.dto.MainWalletResponse;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequest;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentResponse;
import com.wayapay.thirdpartyintegrationservice.dto.TransferFromWalletPojo;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.service.wallet.FundTransferResponse;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
    public boolean secureFund(BigDecimal amount, BigDecimal fee, String userName, String userAccountNumber, String transactionId, FeeBearer feeBearer, String token) throws ThirdPartyIntegrationException {

    	//Get user default wallet
    	MainWalletResponse defaultWallet = walletFeignClient.getDefaultWallet(token);

        //consume
    	TransferFromWalletPojo trans = new TransferFromWalletPojo();
    	trans.setAmount(FeeBearer.CONSUMER.equals(feeBearer) ? amount.add(fee) : amount);
    	trans.setCustomerWalletId(defaultWallet.getId());
    	trans.setPaymentReference("BILLS-PAYMENT-TRANSACTION");
        try {
            walletFeignClient.transferToUser(trans,token);
            return true;
        } catch (FeignException exception) {
            log.error("FeignException => {}", exception.getCause());
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getErrorMessage(exception.contentUTF8()));
        }
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
