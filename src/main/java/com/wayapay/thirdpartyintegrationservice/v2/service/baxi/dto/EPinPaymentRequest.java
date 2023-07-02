package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class EPinPaymentRequest {
    private int numberOfPins;
    private int pinValue;
    @JsonProperty("service_type")
    private String serviceType;
    private int amount;
    private String agentId;
    private String agentReference;
}
