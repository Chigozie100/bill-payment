package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ValidationResponse {
    private String paymentCode;
    private String customerId;
    private String responseCode;
    private String fullName;
    private String amount;
    private String amountType;
    private String amountTypeDescription;
    private String responseDescription;
}