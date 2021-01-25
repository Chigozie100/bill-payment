package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class InternetPaymentResponse extends SuperResponse {
    private InternetPaymentDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class InternetPaymentDetail {
    private String error;
    private String message;
    private String transactionID;
    private String reference;
    private String responseCode;
    private String bundle;
    private String amount;
}
