package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SendPaymentAdviceResponse {

    private String transactionRef;
    private String responseCode;
    private String responseMessage;
    private String responseCodeGrouping;
    private String rechargePIN;
    private String miscData;

}
