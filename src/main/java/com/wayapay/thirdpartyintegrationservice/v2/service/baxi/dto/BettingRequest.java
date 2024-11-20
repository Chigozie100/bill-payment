package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BettingRequest {
    @JsonProperty("service_type")
    private String serviceType;
    @JsonProperty("account_number")
    private String accountNumber;
    private String agentReference;
    private String action;
    private BigDecimal amount;

}
