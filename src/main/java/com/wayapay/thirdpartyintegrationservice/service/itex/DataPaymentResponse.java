package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DataPaymentResponse extends SuperResponse {
    private DataPaymentDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class DataPaymentDetail{
    private String error;
    private String message;
    private String amount;
    private String ref;
    private String date;
    private String transactionID;
    private String responseCode;
    private String description;
    private String reference;
}

