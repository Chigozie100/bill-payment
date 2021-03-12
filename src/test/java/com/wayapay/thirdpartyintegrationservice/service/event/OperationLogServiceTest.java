package com.wayapay.thirdpartyintegrationservice.service.event;

import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.dto.ParamNameValue;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequest;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentResponse;
import com.wayapay.thirdpartyintegrationservice.elasticsearch.OperationLog;
import com.wayapay.thirdpartyintegrationservice.elasticsearch.OperationLogRepo;
import com.wayapay.thirdpartyintegrationservice.event.AnnotationOperation;
import com.wayapay.thirdpartyintegrationservice.event.OperationLogService;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.util.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
//@Testcontainers
class OperationLogServiceTest {

    @Autowired
    private OperationLogRepo operationLogRepo;

    @Mock
    private AnnotationOperation annotationOperation;

    private JoinPoint joinPoint;
    private OperationLogService operationLogService;

    private final String userName = "testUserName";
    private final String sourceAccountNumber = "1111111111";
    private final BigDecimal amount = BigDecimal.ONE;
    private final BigDecimal fee = BigDecimal.ZERO;
    private final String billerId = "TestBiller";
    private final String categoryId = "TestCategory";
    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        operationLogService = new OperationLogService(operationLogRepo, annotationOperation);

        ParamNameValue paramNameValue = new ParamNameValue("testName", "testValue");
        paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(amount);
        paymentRequest.setBillerId(billerId);
        paymentRequest.setCategoryId(categoryId);
        paymentRequest.getData().add(paramNameValue);
        paymentRequest.setSourceWalletAccountNumber(sourceAccountNumber);

        paymentResponse = new PaymentResponse();
        paymentResponse.getData().add(paramNameValue);

        //clear all
//        operationLogRepo.deleteAll();
    }

    @Test
    void testLogOperation() throws NoSuchAlgorithmException {

        //SECURE_FUND -> BigDecimal amount, String userName, String userAccountNumber, String transactionId
        joinPoint = mock(JoinPoint.class);
        AuditPaymentOperation auditPaymentOperation = mock(AuditPaymentOperation.class);
        when(auditPaymentOperation.stage()).thenReturn(Stage.SECURE_FUND);
        when(auditPaymentOperation.status()).thenReturn(Status.START);
        when(annotationOperation.getAnnotation(joinPoint)).thenReturn(auditPaymentOperation);

        Object[] objectsSecureFund = {amount, fee, userName, sourceAccountNumber, CommonUtils.generatePaymentTransactionId()};
        when(joinPoint.getArgs()).thenReturn(objectsSecureFund);
        operationLogService.logOperation(joinPoint, true);
        Optional<OperationLog> optionalOperationLog = operationLogRepo.findByTransactionId(String.valueOf(objectsSecureFund[4]));
        assertTrue(optionalOperationLog.isPresent());
        optionalOperationLog.ifPresent(operationLog -> operationLogRepo.delete(operationLog));

        //CONTACT_VENDOR_TO_PROVIDE_VALUE -> PaymentRequest request, String transactionId, String username
        Object[] objectsContactVendor = {paymentRequest, fee, CommonUtils.generatePaymentTransactionId(), userName};
        when(auditPaymentOperation.stage()).thenReturn(Stage.CONTACT_VENDOR_TO_PROVIDE_VALUE);
        when(auditPaymentOperation.status()).thenReturn(Status.IN_PROGRESS);
        when(joinPoint.getArgs()).thenReturn(objectsContactVendor);
        operationLogService.logOperation(joinPoint, paymentResponse);
        optionalOperationLog = operationLogRepo.findByTransactionId(String.valueOf(objectsContactVendor[2]));
        assertTrue(optionalOperationLog.isPresent());
        optionalOperationLog.ifPresent(operationLog -> operationLogRepo.delete(operationLog));


        //SAVE_TRANSACTION_DETAIL -> PaymentRequest paymentRequest, PaymentResponse paymentResponse, String userName, String transactionId
        Object[] objectsSaveTransaction = {paymentRequest, fee, paymentResponse, userName, CommonUtils.generatePaymentTransactionId()};
        when(auditPaymentOperation.stage()).thenReturn(Stage.SAVE_TRANSACTION_DETAIL);
        when(auditPaymentOperation.status()).thenReturn(Status.END);
        when(joinPoint.getArgs()).thenReturn(objectsSaveTransaction);
        String response = null;
        operationLogService.logOperation(joinPoint, response);
        optionalOperationLog = operationLogRepo.findByTransactionId(String.valueOf(objectsSaveTransaction[4]));
        assertTrue(optionalOperationLog.isPresent());
        optionalOperationLog.ifPresent(operationLog -> operationLogRepo.delete(operationLog));

        //LOG_AS_DISPUTE -> String username, Object request, ThirdPartyNames thirdPartyName, String billerId, String categoryId, BigDecimal amount, String transactionId
        Object[] objectsLogAsDispute = {userName, paymentRequest, ThirdPartyNames.BAXI, billerId, categoryId, amount, fee, CommonUtils.generatePaymentTransactionId()};
        when(auditPaymentOperation.stage()).thenReturn(Stage.LOG_AS_DISPUTE);
        when(auditPaymentOperation.status()).thenReturn(Status.END);
        when(joinPoint.getArgs()).thenReturn(objectsLogAsDispute);
        operationLogService.logOperation(joinPoint, response);
        optionalOperationLog = operationLogRepo.findByTransactionId(String.valueOf(objectsLogAsDispute[7]));
        assertTrue(optionalOperationLog.isPresent());
        optionalOperationLog.ifPresent(operationLog -> operationLogRepo.delete(operationLog));
    }

    @Test
    void testLogOperationWhenExceptionOccurred() throws NoSuchAlgorithmException {

        ThirdPartyIntegrationException thirdPartyIntegrationException = new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        String message = "logging exception";

        //SECURE_FUND -> BigDecimal amount, String userName, String userAccountNumber, String transactionId
        joinPoint = mock(JoinPoint.class);
        AuditPaymentOperation auditPaymentOperation = mock(AuditPaymentOperation.class);
        when(auditPaymentOperation.stage()).thenReturn(Stage.SECURE_FUND);
        when(auditPaymentOperation.status()).thenReturn(Status.START);
        when(annotationOperation.getAnnotation(joinPoint)).thenReturn(auditPaymentOperation);

        Object[] objectsSecureFund = {amount, fee, userName, sourceAccountNumber, CommonUtils.generatePaymentTransactionId()};
        when(joinPoint.getArgs()).thenReturn(objectsSecureFund);
        operationLogService.logOperation(joinPoint, thirdPartyIntegrationException);
        Optional<OperationLog> operationLogSecureFund = operationLogRepo.findByTransactionId(String.valueOf(objectsSecureFund[4]));
        assertAll(message,
                () -> assertTrue(operationLogSecureFund.isPresent()),
                () -> assertEquals(Constants.ERROR_MESSAGE, operationLogSecureFund.orElseGet(OperationLog::new).getResponse()));
        operationLogSecureFund.ifPresent(operationLog -> operationLogRepo.delete(operationLog));

        //CONTACT_VENDOR_TO_PROVIDE_VALUE -> PaymentRequest request, String transactionId, String username
        Object[] objectsContactVendor = {paymentRequest, fee, CommonUtils.generatePaymentTransactionId(), userName};
        when(auditPaymentOperation.stage()).thenReturn(Stage.CONTACT_VENDOR_TO_PROVIDE_VALUE);
        when(auditPaymentOperation.status()).thenReturn(Status.IN_PROGRESS);
        when(joinPoint.getArgs()).thenReturn(objectsContactVendor);
        operationLogService.logOperation(joinPoint, thirdPartyIntegrationException);
        Optional<OperationLog> operationLogContactVendor = operationLogRepo.findByTransactionId(String.valueOf(objectsContactVendor[2]));
        assertAll(message,
                () -> assertTrue(operationLogContactVendor.isPresent()),
                () -> assertEquals(Constants.ERROR_MESSAGE, operationLogContactVendor.orElseGet(OperationLog::new).getResponse()));
        operationLogContactVendor.ifPresent(operationLog -> operationLogRepo.delete(operationLog));

        //SAVE_TRANSACTION_DETAIL -> PaymentRequest paymentRequest, PaymentResponse paymentResponse, String userName, String transactionId
        Object[] objectsSaveTransaction = {paymentRequest, fee, paymentResponse, userName, CommonUtils.generatePaymentTransactionId()};
        when(auditPaymentOperation.stage()).thenReturn(Stage.SAVE_TRANSACTION_DETAIL);
        when(auditPaymentOperation.status()).thenReturn(Status.END);
        when(joinPoint.getArgs()).thenReturn(objectsSaveTransaction);
        operationLogService.logOperation(joinPoint, thirdPartyIntegrationException);
        Optional<OperationLog> operationLogSaveTransaction = operationLogRepo.findByTransactionId(String.valueOf(objectsSaveTransaction[4]));
        assertAll(message,
                () -> assertTrue(operationLogSaveTransaction.isPresent()),
                () -> assertEquals(Constants.ERROR_MESSAGE, operationLogSaveTransaction.orElseGet(OperationLog::new).getResponse()));
        operationLogSaveTransaction.ifPresent(operationLog -> operationLogRepo.delete(operationLog));

        //LOG_AS_DISPUTE -> String username, Object request, ThirdPartyNames thirdPartyName, String billerId, String categoryId, BigDecimal amount, String transactionId
        Object[] objectsLogAsDispute = {userName, paymentRequest, ThirdPartyNames.BAXI, billerId, categoryId, amount, fee, CommonUtils.generatePaymentTransactionId()};
        when(auditPaymentOperation.stage()).thenReturn(Stage.LOG_AS_DISPUTE);
        when(auditPaymentOperation.status()).thenReturn(Status.END);
        when(joinPoint.getArgs()).thenReturn(objectsLogAsDispute);
        operationLogService.logOperation(joinPoint, thirdPartyIntegrationException);
        Optional<OperationLog> operationLogLogAsDispute = operationLogRepo.findByTransactionId(String.valueOf(objectsLogAsDispute[7]));
        assertAll(message,
                () -> assertTrue(operationLogLogAsDispute.isPresent()),
                () -> assertEquals(Constants.ERROR_MESSAGE, operationLogLogAsDispute.orElseGet(OperationLog::new).getResponse()));
        operationLogLogAsDispute.ifPresent(operationLog -> operationLogRepo.delete(operationLog));

    }
}