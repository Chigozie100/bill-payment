package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AirtimePaymentResponse extends SuperResponse {
    private PaymentDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class PaymentDetail {
    private String statusCode;
    private String transactionStatus;
    private String transactionReference;
    private String transactionMessage;
    private String baxiReference;
}