package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequest;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentResponse;
import com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.service.itex.ItexService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BillsPaymentService {

    private final ConfigService configService;
    private final ItexService itexService;
    private final BaxiService baxiService;
    private final QuickTellerService quickTellerService;
    private final PaymentTransactionRepo paymentTransactionRepo;

    public IThirdPartyService getBillsPaymentService() throws ThirdPartyIntegrationException {

        ThirdPartyNames activeThirdParty = configService.getActiveThirdParty();

        switch (activeThirdParty){
            case ITEX:
                return itexService;
            case BAXI:
                return baxiService;
            default:
            case QUICKTELLER:
                return quickTellerService;
        }
    }

    public PaymentResponse processPayment(PaymentRequest paymentRequest, String userName, String userAccountNumber) throws ThirdPartyIntegrationException {

        //secure Payment
        String transactionId = generatePaymentTransactionId();
        if (secureFund(paymentRequest.getAmount(), userName, userAccountNumber, transactionId)){
            PaymentResponse paymentResponse = getBillsPaymentService().processPayment(paymentRequest, transactionId);
            //store the transaction information
            saveTransactionDetail(paymentRequest, paymentResponse, userName, userAccountNumber, transactionId);
            return paymentResponse;
        }

        log.error("Unable to secure fund from user's wallet");
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    //todo
    public boolean secureFund(BigDecimal amount, String userName, String userAccountNumber, String transactionId){
        return true;
    }

    private String generatePaymentTransactionId(){
        return new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + generateRandomNumber();
    }

    public String generateRandomNumber(){
        int lengthOfNumbers = 18;
        String numbers = "";

        for (int i = 0; i < lengthOfNumbers; i++) {
            numbers += (int)(Math.random() * (9 - 0) + 0);
        }

        return numbers;
    }

    private void saveTransactionDetail(PaymentRequest paymentRequest, PaymentResponse paymentResponse, String userName, String userAccountNumber, String transactionId) throws ThirdPartyIntegrationException {
        PaymentTransactionDetail paymentTransactionDetail = new PaymentTransactionDetail();
        paymentTransactionDetail.setAmount(paymentRequest.getAmount());
        paymentTransactionDetail.setBiller(paymentRequest.getBillerId());
        paymentTransactionDetail.setCategory(paymentRequest.getCategoryId());
        paymentTransactionDetail.setPaymentRequest(CommonUtils.ObjectToJson(paymentRequest).orElse(""));
        paymentTransactionDetail.setPaymentResponse(CommonUtils.ObjectToJson(paymentResponse).orElse(""));
        paymentTransactionDetail.setSuccessful(true);
        paymentTransactionDetail.setThirdPartyName(configService.getActiveThirdParty());
        paymentTransactionDetail.setTransactionId(transactionId);
        paymentTransactionDetail.setUserAccountNumber(userAccountNumber);
        paymentTransactionDetail.setUsername(userName);
        paymentTransactionRepo.save(paymentTransactionDetail);
    }

    public Page<TransactionDetail> search(String username, int pageNumber, int pageSize){
        if (CommonUtils.isEmpty(username)){
            return paymentTransactionRepo.getAllTransaction(PageRequest.of(pageNumber, pageSize));
        }
        return paymentTransactionRepo.getAllTransactionByUsername(username, PageRequest.of(pageNumber, pageSize));
    }
}
