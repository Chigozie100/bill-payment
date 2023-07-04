package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceBillerLogoResponse {
    private String status;
    private String message;
    private Integer code;
    private List<ServiceBillerLogoDto> data;
}
