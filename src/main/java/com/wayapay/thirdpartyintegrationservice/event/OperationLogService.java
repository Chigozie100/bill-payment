package com.wayapay.thirdpartyintegrationservice.event;

import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequest;
import com.wayapay.thirdpartyintegrationservice.elasticsearch.OperationLog;
import com.wayapay.thirdpartyintegrationservice.elasticsearch.OperationLogRepo;
import com.wayapay.thirdpartyintegrationservice.util.FinalStatus;
import com.wayapay.thirdpartyintegrationservice.util.Stage;
import com.wayapay.thirdpartyintegrationservice.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Aspect
@RequiredArgsConstructor
@Component
public class OperationLogService {

    private final OperationLogRepo operationLogRepo;
    private final AnnotationOperation annotationOperation;

    @Async
    @AfterReturning(value = "@annotation(com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation)", returning = "response")
    public void logOperation(JoinPoint joinPoint, Object response){
        log.info("Responding =>>>> ");
        Stage stage = getStage(joinPoint);
        log.info("Stage => "+stage);
        switch (stage){
            case SECURE_FUND:
                logSecureFund(joinPoint, response, stage, FinalStatus.COMPLETED);
                break;

            case CONTACT_VENDOR_TO_PROVIDE_VALUE:
                logContactingVendorToProvideValue(joinPoint, response, stage, FinalStatus.COMPLETED);
                break;

            case SAVE_TRANSACTION_DETAIL:
                log.info("running this ........");
                saveTransactionDetail(joinPoint, response, stage, FinalStatus.COMPLETED);
                break;

            case LOG_AS_DISPUTE:
                logAsDispute(joinPoint, response, stage, FinalStatus.COMPLETED);
                break;

            default:
                break;
        }
    }

    @Async
    @AfterThrowing(value = "@annotation(com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation)", throwing = "exception")
    public void logOperation(JoinPoint joinPoint, Exception exception){
        Stage stage = getStage(joinPoint);
        switch (stage){
            case SECURE_FUND:
                logSecureFund(joinPoint, exception.getMessage(), stage, FinalStatus.EXCEPTION_OCCURRED);
                break;

            case CONTACT_VENDOR_TO_PROVIDE_VALUE:
                logContactingVendorToProvideValue(joinPoint, exception.getMessage(), stage, FinalStatus.EXCEPTION_OCCURRED);
                break;

            case SAVE_TRANSACTION_DETAIL:
                saveTransactionDetail(joinPoint, exception.getMessage(), stage, FinalStatus.EXCEPTION_OCCURRED);
                break;

            case LOG_AS_DISPUTE:
                logAsDispute(joinPoint, exception.getMessage(), stage, FinalStatus.EXCEPTION_OCCURRED);
                break;

            default:
                break;
        }
    }

    private void logSecureFund(JoinPoint joinPoint, Object response,
                               Stage stage, FinalStatus finalStatus){

//        BigDecimal amount, String userName, String userAccountNumber, String transactionId
        OperationLog operationLog = new OperationLog();
        operationLog.setAmount((BigDecimal)joinPoint.getArgs()[0]);
        operationLog.setFinalStatus(finalStatus);
        operationLog.setSourceAccountNumber(String.valueOf(joinPoint.getArgs()[2]));
        operationLog.setStage(stage);
        operationLog.setStatus(getStatus(joinPoint));
        operationLog.setTransactionId(String.valueOf(joinPoint.getArgs()[3]));
        operationLog.setTransactionType("BillPayment");
        operationLog.setUserId(String.valueOf(joinPoint.getArgs()[1]));
        operationLog.setResponse(String.valueOf(response));
        try {
            operationLogRepo.save(operationLog);
        } catch (Exception exception) {
            log.error("[logSecureFund] : Unable to save payment operation into elasticsearch", exception);
        }
    }

    private void logContactingVendorToProvideValue(JoinPoint joinPoint, Object response,
                                                   Stage stage, FinalStatus finalStatus){
//        PaymentRequest request, String transactionId, String username
        PaymentRequest paymentRequest = (PaymentRequest) joinPoint.getArgs()[0];
        OperationLog operationLog = new OperationLog();
        operationLog.setAmount(paymentRequest.getAmount());
        operationLog.setFinalStatus(finalStatus);
        operationLog.setSourceAccountNumber(paymentRequest.getSourceWalletAccountNumber());
        operationLog.setStage(stage);
        operationLog.setStatus(getStatus(joinPoint));
        operationLog.setTransactionId(String.valueOf(joinPoint.getArgs()[1]));
        operationLog.setTransactionType("BillPayment - "+paymentRequest.getBillerId()+" - "+paymentRequest.getCategoryId());
        operationLog.setUserId(String.valueOf(joinPoint.getArgs()[2]));
        operationLog.setResponse(String.valueOf(response));
        try {
            operationLogRepo.save(operationLog);
        } catch (Exception exception) {
            log.error("[logContactingVendorToProvideValue] : Unable to save payment operation into elasticsearch", exception);
        }
    }

    private void saveTransactionDetail(JoinPoint joinPoint, Object response,
                                                   Stage stage, FinalStatus finalStatus){
//        PaymentRequest paymentRequest, PaymentResponse paymentResponse, String userName, String transactionId
        PaymentRequest paymentRequest = (PaymentRequest) joinPoint.getArgs()[0];
        OperationLog operationLog = new OperationLog();
        operationLog.setAmount(paymentRequest.getAmount());
        operationLog.setFinalStatus(finalStatus);
        operationLog.setSourceAccountNumber(paymentRequest.getSourceWalletAccountNumber());
        operationLog.setStage(stage);
        operationLog.setStatus(getStatus(joinPoint));
        operationLog.setTransactionId(String.valueOf(joinPoint.getArgs()[3]));
        operationLog.setTransactionType("BillPayment - "+paymentRequest.getBillerId()+" - "+paymentRequest.getCategoryId());
        operationLog.setUserId(String.valueOf(joinPoint.getArgs()[2]));
        if (!Objects.isNull(response)){
            operationLog.setResponse(String.valueOf(response));
        }

        try {
            operationLogRepo.save(operationLog);
        } catch (Exception exception) {
            log.error("[saveTransactionDetail] : Unable to save payment operation into elasticsearch", exception);
        }
    }

    private void logAsDispute(JoinPoint joinPoint, Object response,
                                 Stage stage, FinalStatus finalStatus){

//        String username, Object request, ThirdPartyNames thirdPartyName,
//                String billerId, String categoryId, BigDecimal amount, String transactionId
        PaymentRequest paymentRequest = (PaymentRequest) joinPoint.getArgs()[1];
        OperationLog operationLog = new OperationLog();
        operationLog.setAmount((BigDecimal) joinPoint.getArgs()[5]);
        operationLog.setFinalStatus(finalStatus);
        operationLog.setSourceAccountNumber(paymentRequest.getSourceWalletAccountNumber());
        operationLog.setStage(stage);
        operationLog.setStatus(getStatus(joinPoint));
        operationLog.setTransactionId(String.valueOf(joinPoint.getArgs()[6]));
        operationLog.setTransactionType("BillPayment - "+paymentRequest.getBillerId()+" - "+paymentRequest.getCategoryId());
        operationLog.setUserId(String.valueOf(joinPoint.getArgs()[0]));
        if (!Objects.isNull(response)) {
            operationLog.setResponse(String.valueOf(response));
        }
        try {
            operationLogRepo.save(operationLog);
        } catch (Exception exception) {
            log.error("[logAsDispute] : Unable to save payment operation into elasticsearch", exception);
        }
    }

    private Status getStatus(JoinPoint joinPoint){
        return annotationOperation.getAnnotation(joinPoint).status();
    }

    private Stage getStage(JoinPoint joinPoint){
        return annotationOperation.getAnnotation(joinPoint).stage();
    }

}
