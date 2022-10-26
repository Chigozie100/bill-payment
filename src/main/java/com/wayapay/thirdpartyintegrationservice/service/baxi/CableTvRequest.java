package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CableTvRequest {
    @JsonProperty("service_type")
    private String serviceType;
}
