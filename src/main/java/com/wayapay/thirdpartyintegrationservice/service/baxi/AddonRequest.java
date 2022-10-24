package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AddonRequest {
    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("product_code")
    private String productCode;
}

