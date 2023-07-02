package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElectricRawOutPut {
    private String status;
    private BigDecimal costOfUnit;
    private BigDecimal taxAmount;
    private BigDecimal debtAmount;
    private BigDecimal tokenAmount;
    private String creditToken;
    private String resetToken;
    private String amountOfPower;
    private String exchangeReference;
    private BigDecimal tariffBaseRate;
    private String account;
    private String receiptNumber;
    private String discoExchangeReference;
    private String token;
}
