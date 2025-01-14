package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EPinRequest {
    @JsonProperty("service_type")
    private String serviceType;
    private String numberOfPins;
    private String pinValue;
    private String amount;
    private String agentId;
    private String agentReference;
}
