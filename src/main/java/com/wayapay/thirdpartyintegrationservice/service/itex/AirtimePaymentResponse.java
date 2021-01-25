package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AirtimePaymentResponse extends SuperResponse {
    private AirtimePaymentDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class AirtimePaymentDetail{
    private String error;
    private String message;
    private String amount;
    private String ref;
    private String date;
    private String transactionID;
    private String value;
    private String token;
    private String response;
    private String responseCode;
    private String reference;
    private String sequence;
    private String clientReference;
}
