package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ValidateBettingAccount {
    @JsonProperty("service_type")
    private String serviceType;
    @JsonProperty("account_number")
    private String accountNumber;
}