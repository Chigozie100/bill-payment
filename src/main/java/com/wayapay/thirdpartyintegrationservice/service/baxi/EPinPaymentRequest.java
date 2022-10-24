package com.wayapay.thirdpartyintegrationservice.service.baxi;

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
    private String numberOfPins;
    private String pinValue;
    @JsonProperty("service_type")
    private String serviceType;
    private String amount;
    private String agentId;
    private String agentReference;
}
