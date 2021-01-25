package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ElectricityPaymentResponse extends SuperResponse {
    private ElectricityPaymentDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class ElectricityPaymentDetail{
    private String error;
    private String account;
    private String name;
    private String token;
    private String accountNumber;
    private String phone;
    private String email;
    private String address;
    private String minimumPurchase;
    private String businessUnit;
    private String businessUnitId;
    private String undertaking;
    private String customerArrears;
    private String tariffCode;
    private String tariff;
    private String paidamount;
    private String merchantId;
    private String recieptNumber;
    private String transactionDate;
    private String transactionReference;
    private String transactionStatus;
    private String message;
    private String description;
    private String externalReference;
    private String type;
    private String units;
    private String vat;
    private String costofunit;
    private String lastTransactionDate;
    private String responseCode;
    private String amount;
    private String reference;
    private String sequence;
    private String clientReference;
}
