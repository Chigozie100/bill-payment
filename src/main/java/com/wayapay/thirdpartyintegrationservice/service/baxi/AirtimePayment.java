package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirtimePayment {
    private String phone;
    private String amount;
    @JsonProperty("service_type")
    private String serviceType;
    private String plan;
    private String agentId;
    private String agentReference;


}
