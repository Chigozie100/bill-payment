package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class QueryTransactionResponse {

    private BillPaymentDto billPayment;
    private Recharge recharge;
    private String amount;
    private String currencyCode;
    private String customer;
    private String customerEmail;
    private String paymentDate;
    private String requestReference;
    private String serviceCode;
    private String serviceName;
    private String serviceProviderId;
    private String status;
    private String surcharge;
    private String transactionRef;
    private String transactionResponseCode;
    private String transactionSet;
    private String responseCode;
}

