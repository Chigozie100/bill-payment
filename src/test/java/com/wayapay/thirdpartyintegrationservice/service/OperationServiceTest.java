package com.wayapay.thirdpartyintegrationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.service.wallet.FundTransferRequest;
import com.wayapay.thirdpartyintegrationservice.service.wallet.FundTransferResponse;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.FeeBearer;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@DirtiesContext
@SpringBootTest
class OperationServiceTest {

    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;
    @MockBean
    private CategoryService categoryService;
    @Mock
    private WalletFeignClient walletFeignClient;
    private OperationService operationService;
    private static final String testUserName = "testUserName";

    @BeforeEach
    void setUp() {
        operationService = new OperationService(paymentTransactionRepo, walletFeignClient, categoryService);
    }

    @Test
    void secureFund() throws ThirdPartyIntegrationException, NoSuchAlgorithmException, JsonProcessingException, URISyntaxException {

        String sourceUserAccount = "111111111";
        String sampleToken = "serial eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjZWNpbGlha25kQGdtYWlsLmNvbSIsImV4cCI6MTY1MjI3MTc0NH0.31hPp08wDKB7HiJMtSkI-gX0ppjm2QJx0SXhO2WsK_g";
        when(categoryService.findThirdPartyByCategoryAggregatorCode(anyString())).thenReturn(Optional.of(ThirdPartyNames.BAXI));
        when(walletFeignClient.transferToUser(any(TransferFromWalletPojo.class), anyString())).thenReturn(new TransactionRequest(Long.parseLong("1"),"sample Payment Reference", "sample Payment Description", BigDecimal.ONE));
        when(walletFeignClient.getDefaultWallet(anyString())).thenReturn(new MainWalletResponse(Long.parseLong("1")));
        assertTrue(operationService.secureFund(BigDecimal.ONE, BigDecimal.ZERO, testUserName, sourceUserAccount, CommonUtils.generatePaymentTransactionId(), FeeBearer.BILLER, sampleToken));

        when(walletFeignClient.transferToUser(any(TransferFromWalletPojo.class), anyString())).thenThrow(new FeignException.FeignClientException(HttpStatus.BAD_GATEWAY.value(), Constants.ERROR_MESSAGE, Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), "body".getBytes(), Charset.defaultCharset(), new RequestTemplate()), CommonUtils.getObjectMapper().writeValueAsString(new ResponseHelper(false, "test", "")).getBytes()));
        assertThrows(ThirdPartyIntegrationException.class, () -> operationService.secureFund(BigDecimal.ONE, BigDecimal.ZERO, testUserName, sourceUserAccount, CommonUtils.generatePaymentTransactionId(), FeeBearer.CONSUMER, sampleToken));

    }

    @Test
    void saveTransactionDetail() throws ThirdPartyIntegrationException {
        when(categoryService.findThirdPartyByCategoryAggregatorCode(anyString())).thenReturn(Optional.of(ThirdPartyNames.BAXI));
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setBillerId("testBillerId");
        paymentRequest.setCategoryId("testCategoryId");
        paymentRequest.setSourceWalletAccountNumber("testAccountNumber");
        assertDoesNotThrow(() -> operationService.saveTransactionDetail(paymentRequest, BigDecimal.ZERO, new PaymentResponse(), testUserName, CommonUtils.generatePaymentTransactionId()));
    }
}