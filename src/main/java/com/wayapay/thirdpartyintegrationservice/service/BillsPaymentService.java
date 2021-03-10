package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequest;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentResponse;
import com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.service.dispute.DisputeService;
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
import java.security.NoSuchAlgorithmException;

@Slf4j
@RequiredArgsConstructor
@Service
public class BillsPaymentService {

    private final ConfigService configService;
    private final ItexService itexService;
    private final BaxiService baxiService;
    private final QuickTellerService quickTellerService;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final DisputeService disputeService;
    private final OperationService operationService;
    private final BillerConsumerFeeService billerConsumerFeeService;

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

    //todo fee needs to be considered while securing funds
    public PaymentResponse processPayment(PaymentRequest paymentRequest, String userName) throws ThirdPartyIntegrationException {

        //secure Payment
        String transactionId = null;
        try {
            transactionId = CommonUtils.generatePaymentTransactionId();
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

        BigDecimal fee = billerConsumerFeeService.getFee(paymentRequest.getAmount(), configService.getActiveThirdParty(), paymentRequest.getBillerId());
        if (operationService.secureFund(paymentRequest.getAmount(), fee, userName, paymentRequest.getSourceWalletAccountNumber(), transactionId)){
            try {
                PaymentResponse paymentResponse = getBillsPaymentService().processPayment(paymentRequest, fee, transactionId, userName);
                //store the transaction information
                operationService.saveTransactionDetail(paymentRequest, fee, paymentResponse, userName, transactionId);
                return paymentResponse;
            } catch (ThirdPartyIntegrationException e) {
                disputeService.logTransactionAsDispute(userName, paymentRequest, configService.getActiveThirdParty(), paymentRequest.getBillerId(), paymentRequest.getCategoryId(), paymentRequest.getAmount(), fee, transactionId);
                throw new ThirdPartyIntegrationException(e.getHttpStatus(), e.getMessage());
            }
        }

        log.error("Unable to secure fund from user's wallet");
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    public Page<TransactionDetail> search(String username, int pageNumber, int pageSize){
        if (CommonUtils.isEmpty(username)){
            return paymentTransactionRepo.getAllTransaction(PageRequest.of(pageNumber, pageSize));
        }
        return paymentTransactionRepo.getAllTransactionByUsername(username, PageRequest.of(pageNumber, pageSize));
    }
}
