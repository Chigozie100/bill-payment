package com.wayapay.thirdpartyintegrationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequest;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.service.wallet.FundTransferRequest;
import com.wayapay.thirdpartyintegrationservice.service.wallet.WalletFeignClient;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
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

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
class OperationServiceTest {

    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;
    @MockBean
    private ConfigService configService;
    @Mock
    private WalletFeignClient walletFeignClient;
    private OperationService operationService;
    private static final String testUserName = "testUserName";

    @BeforeEach
    void setUp() {
        operationService = new OperationService(paymentTransactionRepo, configService, walletFeignClient);
    }

    @Test
    void secureFund() throws ThirdPartyIntegrationException, NoSuchAlgorithmException, JsonProcessingException {

        String sourceUserAccount = "111111111";
        when(configService.getThirdPartyAccountNumber()).thenReturn("222233333");
        when(walletFeignClient.wallet2wallet(Mockito.any(FundTransferRequest.class))).thenReturn(new ResponseHelper(true, "test", ""));
        assertTrue(operationService.secureFund(BigDecimal.ONE, BigDecimal.ZERO, testUserName, sourceUserAccount, CommonUtils.generatePaymentTransactionId()));

        when(walletFeignClient.wallet2wallet(Mockito.any(FundTransferRequest.class))).thenThrow(new FeignException.FeignClientException(HttpStatus.BAD_GATEWAY.value(), Constants.ERROR_MESSAGE, Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), "body".getBytes(), Charset.defaultCharset(), new RequestTemplate()), CommonUtils.getObjectMapper().writeValueAsString(new ResponseHelper(false, "test", "")).getBytes()));
        assertThrows(ThirdPartyIntegrationException.class, () -> operationService.secureFund(BigDecimal.ONE, BigDecimal.ZERO, testUserName, sourceUserAccount, CommonUtils.generatePaymentTransactionId()));

    }

    @Test
    void saveTransactionDetail() throws NoSuchAlgorithmException, ThirdPartyIntegrationException {
        when(configService.getActiveThirdParty()).thenReturn(ThirdPartyNames.BAXI);
        operationService.saveTransactionDetail(new PaymentRequest(), BigDecimal.ZERO, new PaymentResponse(), testUserName, CommonUtils.generatePaymentTransactionId());
    }
}