package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString @JsonIgnoreProperties(ignoreUnknown = true)
public class ElectricPaymentRequest {
    private String account_number;
    private BigDecimal amount;
    private String phone;
    private String service_type;
    private String agentId;
    private String metadata;
    private String agentReference;
}
