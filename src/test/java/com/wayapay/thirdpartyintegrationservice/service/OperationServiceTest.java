//package com.wayapay.thirdpartyintegrationservice.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.wayapay.thirdpartyintegrationservice.dto.*;
//import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
//import com.wayapay.thirdpartyintegrationservice.repo.BillsPaymentRefundRepository;
//import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
//import com.wayapay.thirdpartyintegrationservice.repo.TransactionTrackerRepository;
//import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
//import com.wayapay.thirdpartyintegrationservice.service.logactivity.LogFeignClient;
//import com.wayapay.thirdpartyintegrationservice.service.notification.NotificationFeignClient;
//import com.wayapay.thirdpartyintegrationservice.service.profile.ProfileFeignClient;
//import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;
//import com.wayapay.thirdpartyintegrationservice.service.referral.ReferralFeignClient;
//import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
//import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
//import com.wayapay.thirdpartyintegrationservice.util.Constants;
//import com.wayapay.thirdpartyintegrationservice.util.FeeBearer;
//import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
//import feign.FeignException;
//import feign.Request;
//import feign.RequestTemplate;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpStatus;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.test.annotation.DirtiesContext;
//
//import java.math.BigDecimal;
//import java.net.URISyntaxException;
//import java.nio.charset.Charset;
//import java.security.NoSuchAlgorithmException;
//import java.util.HashMap;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//import static org.mockito.Mockito.when;
//
//@DirtiesContext
//@SpringBootTest
//class OperationServiceTest {
//
//    @Mock
//    private PaymentTransactionRepo paymentTransactionRepo;
//    @MockBean
//    private CategoryService categoryService;
//    @Mock
//    private WalletFeignClient walletFeignClient;
//    private OperationService operationService;
//    private ProfileFeignClient profileFeignClient;
//    private NotificationFeignClient notificationFeignClient;
//    private LogFeignClient logFeignClient;
//    private BillsPaymentRefundRepository billsPaymentRefundRepository;
//    private static final String testUserName = "10";
//    private TransactionTrackerRepository transactionTrackerRepository;
//    private ReferralFeignClient referralFeignClient;
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    @BeforeEach
//    void setUp() {
//        operationService = new OperationService(paymentTransactionRepo, walletFeignClient, categoryService, profileFeignClient, notificationFeignClient, logFeignClient, billsPaymentRefundRepository,transactionTrackerRepository,referralFeignClient,kafkaTemplate);
//    }
//
//    @Test
//    void secureFund() throws ThirdPartyIntegrationException, NoSuchAlgorithmException, JsonProcessingException {
//
//        String sourceUserAccount = "992";
//        String sampleToken = "serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZ2JlLnRlcnNlZXJAZ21haWwuY29tIiwiZXhwIjoxNjYxMzc0NDA0fQ.PxESICAYmiBTNf622maXdlyy0cJs3nX6QIe88OY_7Sw";
//        when(categoryService.findThirdPartyByCategoryAggregatorCode(anyString())).thenReturn(Optional.of(ThirdPartyNames.BAXI));
//        assertDoesNotThrow(() -> walletFeignClient.transferToUser(any(TransferFromWalletPojo.class), anyString()));
//        assertDoesNotThrow(() ->walletFeignClient.getDefaultWallet(testUserName,sampleToken));
//        assertThrows(NullPointerException.class, () ->operationService.secureFund(BigDecimal.ONE, BigDecimal.ZERO, testUserName, sourceUserAccount, CommonUtils.generatePaymentTransactionId(), FeeBearer.BILLER, sampleToken));
//        when(walletFeignClient.transferFromUserToWaya(any(TransferFromWalletPojo.class), anyString())).thenThrow(new FeignException.FeignClientException(HttpStatus.BAD_GATEWAY.value(), Constants.ERROR_MESSAGE, Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), "body".getBytes(), Charset.defaultCharset(), new RequestTemplate()), CommonUtils.getObjectMapper().writeValueAsString(new ResponseHelper(false, "test", "")).getBytes()));
//        assertThrows(NullPointerException.class, () -> operationService.secureFund(BigDecimal.ONE, BigDecimal.ZERO, testUserName, sourceUserAccount, CommonUtils.generatePaymentTransactionId(), FeeBearer.CONSUMER, sampleToken));
//
//    }
//
//    @Test
//    void saveTransactionDetail() throws ThirdPartyIntegrationException {
//        when(categoryService.findThirdPartyByCategoryAggregatorCode(anyString())).thenReturn(Optional.of(ThirdPartyNames.BAXI));
//        PaymentRequest paymentRequest = new PaymentRequest();
//        paymentRequest.setAmount(new BigDecimal("100.00"));
//        paymentRequest.setBillerId("testBillerId");
//        paymentRequest.setCategoryId("testCategoryId");
//        paymentRequest.setSourceWalletAccountNumber("testAccountNumber");
//        UserProfileResponse userProfileResponse = new UserProfileResponse();
//        userProfileResponse.setFirstName("Terseer");
//        userProfileResponse.setUserId("10");
//        assertDoesNotThrow(() -> operationService.saveTransactionDetail(userProfileResponse, paymentRequest, BigDecimal.ZERO, new PaymentResponse(), testUserName, CommonUtils.generatePaymentTransactionId()));
//    }
//}