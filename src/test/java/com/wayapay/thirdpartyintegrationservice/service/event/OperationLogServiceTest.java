package com.wayapay.thirdpartyintegrationservice.service.event;

import com.wayapay.thirdpartyintegrationservice.annotations.AuditPaymentOperation;
import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.dto.ParamNameValue;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequest;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentResponse;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@Slf4j
//@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
//@DirtiesContext
//@SpringBootTest
class OperationLogServiceTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AppConfig appConfig;

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

//    @BeforeEach
    void setUp() {
        operationLogService = new OperationLogService(annotationOperation, kafkaTemplate, appConfig);

        ParamNameValue paramNameValue = new ParamNameValue("testName", "testValue");
        paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(amount);
        paymentRequest.setBillerId(billerId);
        paymentRequest.setCategoryId(categoryId);
        paymentRequest.getData().add(paramNameValue);
        paymentRequest.setSourceWalletAccountNumber(sourceAccountNumber);

        paymentResponse = new PaymentResponse();
        paymentResponse.getData().add(paramNameValue);

    }

//    @Test
    void testLogOperation() throws NoSuchAlgorithmException {

        //SECURE_FUND -> BigDecimal amount, String userName, String userAccountNumber, String transactionId
        joinPoint = mock(JoinPoint.class);
        AuditPaymentOperation auditPaymentOperation = mock(AuditPaymentOperation.class);
        when(auditPaymentOperation.stage()).thenReturn(Stage.SECURE_FUND);
        when(auditPaymentOperation.status()).thenReturn(Status.START);
        when(annotationOperation.getAnnotation(joinPoint)).thenReturn(auditPaymentOperation);

        Object[] objectsSecureFund = {amount, fee, userName, sourceAccountNumber, CommonUtils.generatePaymentTransactionId()};
        when(joinPoint.getArgs()).thenReturn(objectsSecureFund);
        assertDoesNotThrow(() -> operationLogService.logOperation(joinPoint, true));

        //CONTACT_VENDOR_TO_PROVIDE_VALUE -> PaymentRequest request, String transactionId, String username
        Object[] objectsContactVendor = {paymentRequest, fee, CommonUtils.generatePaymentTransactionId(), userName};
        when(auditPaymentOperation.stage()).thenReturn(Stage.CONTACT_VENDOR_TO_PROVIDE_VALUE);
        when(auditPaymentOperation.status()).thenReturn(Status.IN_PROGRESS);
        when(joinPoint.getArgs()).thenReturn(objectsContactVendor);
        assertDoesNotThrow(() -> operationLogService.logOperation(joinPoint, paymentResponse));


        //SAVE_TRANSACTION_DETAIL -> PaymentRequest paymentRequest, PaymentResponse paymentResponse, String userName, String transactionId
        Object[] objectsSaveTransaction = {paymentRequest, fee, paymentResponse, userName, CommonUtils.generatePaymentTransactionId()};
        when(auditPaymentOperation.stage()).thenReturn(Stage.SAVE_TRANSACTION_DETAIL);
        when(auditPaymentOperation.status()).thenReturn(Status.END);
        when(joinPoint.getArgs()).thenReturn(objectsSaveTransaction);
        String response = null;
        assertDoesNotThrow(() -> operationLogService.logOperation(joinPoint, response));

        //LOG_AS_DISPUTE -> String username, Object request, ThirdPartyNames thirdPartyName, String billerId, String categoryId, BigDecimal amount, String transactionId
        Object[] objectsLogAsDispute = {userName, paymentRequest, ThirdPartyNames.BAXI, billerId, categoryId, amount, fee, CommonUtils.generatePaymentTransactionId()};
        when(auditPaymentOperation.stage()).thenReturn(Stage.LOG_AS_DISPUTE);
        when(auditPaymentOperation.status()).thenReturn(Status.END);
        when(joinPoint.getArgs()).thenReturn(objectsLogAsDispute);
        assertDoesNotThrow(() -> operationLogService.logOperation(joinPoint, response));
    }

//    @Test
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
        assertDoesNotThrow(() -> operationLogService.logOperation(joinPoint, thirdPartyIntegrationException));

        //CONTACT_VENDOR_TO_PROVIDE_VALUE -> PaymentRequest request, String transactionId, String username
        Object[] objectsContactVendor = {paymentRequest, fee, CommonUtils.generatePaymentTransactionId(), userName};
        when(auditPaymentOperation.stage()).thenReturn(Stage.CONTACT_VENDOR_TO_PROVIDE_VALUE);
        when(auditPaymentOperation.status()).thenReturn(Status.IN_PROGRESS);
        when(joinPoint.getArgs()).thenReturn(objectsContactVendor);
        assertDoesNotThrow(() -> operationLogService.logOperation(joinPoint, thirdPartyIntegrationException));

        //SAVE_TRANSACTION_DETAIL -> PaymentRequest paymentRequest, PaymentResponse paymentResponse, String userName, String transactionId
        Object[] objectsSaveTransaction = {paymentRequest, fee, paymentResponse, userName, CommonUtils.generatePaymentTransactionId()};
        when(auditPaymentOperation.stage()).thenReturn(Stage.SAVE_TRANSACTION_DETAIL);
        when(auditPaymentOperation.status()).thenReturn(Status.END);
        when(joinPoint.getArgs()).thenReturn(objectsSaveTransaction);
        assertDoesNotThrow(() -> operationLogService.logOperation(joinPoint, thirdPartyIntegrationException));

        //LOG_AS_DISPUTE -> String username, Object request, ThirdPartyNames thirdPartyName, String billerId, String categoryId, BigDecimal amount, String transactionId
        Object[] objectsLogAsDispute = {userName, paymentRequest, ThirdPartyNames.BAXI, billerId, categoryId, amount, fee, CommonUtils.generatePaymentTransactionId()};
        when(auditPaymentOperation.stage()).thenReturn(Stage.LOG_AS_DISPUTE);
        when(auditPaymentOperation.status()).thenReturn(Status.END);
        when(joinPoint.getArgs()).thenReturn(objectsLogAsDispute);
        assertDoesNotThrow(() -> operationLogService.logOperation(joinPoint, thirdPartyIntegrationException));

    }
}