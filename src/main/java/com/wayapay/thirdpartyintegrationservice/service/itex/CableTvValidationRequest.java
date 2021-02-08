package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CableTvValidationRequest {
    private String service;
    private String channel;
    private String type;
    private String account;
    private String amount;
    private String smartCardCode;
}
