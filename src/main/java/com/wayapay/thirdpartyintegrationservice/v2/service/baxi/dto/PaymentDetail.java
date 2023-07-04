package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDetail {
    private String statusCode;
    private String transactionStatus;
    private String transactionReference;
    private String transactionMessage;
    private String baxiReference;
    private String provider_message;
    private BundleExtraData extraData;
}
