package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BettingRequest {
    @JsonProperty("service_type")
    private String serviceType;
    @JsonProperty("account_number")
    private String accountNumber;
    private String agentReference;
    private String action;
    private String amount;

}
