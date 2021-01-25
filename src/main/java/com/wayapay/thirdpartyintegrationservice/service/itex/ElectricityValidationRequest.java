package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ElectricityValidationRequest {

    private String meterNo;
    private String accountType;
    private String service;
    private String amount;
    private String channel;

}
