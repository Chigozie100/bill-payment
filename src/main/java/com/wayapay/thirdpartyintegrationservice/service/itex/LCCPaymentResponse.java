package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class LCCPaymentResponse extends SuperResponse {
    private LCCPaymentDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class LCCPaymentDetail {
    private Boolean error;
    private String message;
    private String description;
    private String name;
    private String account;
    private String type;
    private String paymentLogID;
    private String ref;
    private String amount;
    private String receipt_no;
    private String channel;
    private String date;
    private String transactionID;
    private String responseCode;
    private String reference;
    private String sequence;
    private String clientReference;
}
