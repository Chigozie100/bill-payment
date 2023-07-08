package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString @JsonIgnoreProperties(ignoreUnknown = true)
public class SendPaymentAdviceResponse extends ErrorQtDto {

    private String transactionRef;
    private String responseCode;
    private String responseMessage;
    private String responseCodeGrouping;
    private String rechargePIN;
    private String miscData;

}
