package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AddonRequest {
    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("product_code")
    private String productCode;
}

