package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AirtimePaymentRequest {
    private String phone;
    private String paymentMethod;
    private String service;
    private String amount;
    private String clientReference;
    private String pin;
    private String channel;
}
