package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SendPaymentAdviceRequest {
    private String terminalId;
    private String paymentCode;
    private String customerId;
    private String customerMobile;
    private String customerEmail;
    private String amount;
    private String requestReference;

}
