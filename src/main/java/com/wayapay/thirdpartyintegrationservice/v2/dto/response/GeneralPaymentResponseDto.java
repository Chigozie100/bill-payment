package com.wayapay.thirdpartyintegrationservice.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto.GeneralEpinData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralPaymentResponseDto {
    private String statusCode;
    private String statusMessage;
    private String transactionMessage;
    private String transactionReference;
    private String providerReference;
    private String providerMessage;
    private String purchasedDuration;
    private String purchasedPackage;
    private String voucherCode;
    private String captureUrl;
    private String tokenCode;
    private BigDecimal tokenAmount;
    private String amountOfPower;
    private String creditToken;
    private List<GeneralEpinData> pins;
    private String exchangeReference;
    private String transactionNumber;
}
