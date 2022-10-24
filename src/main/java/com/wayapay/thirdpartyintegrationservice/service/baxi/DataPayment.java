package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataPayment {
    private String phone;
    private String amount;
    @JsonProperty("service_type")
    private String serviceType;
    private String datacode;
    private String agentId;
    private String agentReference;

}
