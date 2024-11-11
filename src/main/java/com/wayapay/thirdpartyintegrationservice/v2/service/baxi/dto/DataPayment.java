package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DataPayment {
    private String phone;
    private BigDecimal amount;
    @JsonProperty("service_type")
    private String serviceType;
    private String datacode;
    private String agentId;
    private String agentReference;

}
