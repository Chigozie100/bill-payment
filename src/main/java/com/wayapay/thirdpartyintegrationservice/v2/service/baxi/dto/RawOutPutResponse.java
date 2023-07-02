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
public class RawOutPutResponse {
    private String email;
    private String tariffCode;
    private String meterNumber;
    private String phoneNumber;
    private String undertaking;
    private String businessUnit;
    private String customerName;
    private String accountNumber;
    private double customerArrears;
    private String minimumPurchase;
}
