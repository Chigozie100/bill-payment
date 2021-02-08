package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RemitaValidationResponse extends SuperResponse {
    private RemitaDetails data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class RemitaDetails{
    private Boolean error;
    private String responseCode;
    private String message;
    private String appVersionCode;
    private List<RemitaDetail> data;
    private String productCode;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class RemitaDetail{
    private String status;
    private String paymentStatus;
    private String rrr;
    private String amountDue;
    private String chargeFee;
    private String rrrAmount;
    private String payerEmail;
    private String payerName;
    private String payerPhone;
    private String description;
    private String currency;
    private String type;
    private String acceptPartPayment;
    private String frequency;
    private String payerAccountNumber;
    private String maxNoOfDebits;
    private String startDate;
    private String endDate;
}
