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
public class ElectricDetail {
    private String transactionStatus;
    private String transactionReference;
    private String statusCode;
    private String transactionMessage;
    private String tokenCode;
    private BigDecimal tokenAmount;
    private String amountOfPower;
    private String creditToken;
    private String resetToken;
    private String configureToken;
    private String baxiReference;
    private String provider_message;
    private ElectricRawOutPut rawOutput;
}
