package com.wayapay.thirdpartyintegrationservice.service.dispute;

import com.wayapay.thirdpartyintegrationservice.dto.PaymentRequest;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
class DisputeServiceTest {

    @MockBean
    private DisputeServiceFeignClient disputeServiceFeignClient;

    private DisputeService disputeService;

    @BeforeEach
    void setUp() {
        disputeService = new DisputeService(disputeServiceFeignClient);
    }

    @Test
    void logTransactionAsDispute() {
        DisputeResponse disputeResponse = new DisputeResponse();
        disputeResponse.setMessage("dispute created successfully");
        disputeResponse.setStatus(true);
        disputeResponse.setTimestamp("2021-03-07T20:31:51.964799Z");
        Mockito.when(disputeServiceFeignClient.logTransactionAsDispute(Mockito.any(DisputeRequest.class))).thenReturn(disputeResponse);
        Assertions.assertDoesNotThrow(() -> disputeService.logTransactionAsDispute("testUser", new PaymentRequest(), ThirdPartyNames.BAXI, "mtn", "airtime", BigDecimal.ONE, BigDecimal.ZERO, "testTransactionId"));

        Mockito.when(disputeServiceFeignClient.logTransactionAsDispute(Mockito.any(DisputeRequest.class))).thenThrow(NullPointerException.class);
        Assertions.assertDoesNotThrow(() -> disputeService.logTransactionAsDispute("testUser", new PaymentRequest(), ThirdPartyNames.BAXI, "mtn", "airtime", BigDecimal.ONE, BigDecimal.ZERO, "testTransactionId"));
    }
}