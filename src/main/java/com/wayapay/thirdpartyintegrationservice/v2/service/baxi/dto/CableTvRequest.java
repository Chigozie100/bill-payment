package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CableTvRequest {
    @JsonProperty("service_type")
    private String serviceType;
}
