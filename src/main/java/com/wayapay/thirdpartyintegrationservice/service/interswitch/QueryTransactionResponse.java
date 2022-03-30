package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import lombok.Data;

@Data
public class QueryTransactionResponse {

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

@Data
class Recharge{
    private String biller;
    private String customerId1;
    private String customerId2;
    private String paymentTypeName;
    private String paymentTypeCode;
    private String billerId;

}
