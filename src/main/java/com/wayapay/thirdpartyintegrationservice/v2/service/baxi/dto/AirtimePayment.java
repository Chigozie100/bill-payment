package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirtimePayment {
    private String phone;
    private int amount;
    @JsonProperty("service_type")
    private String serviceType;
    private String plan;
    private String agentId;
    private String agentReference;

}
