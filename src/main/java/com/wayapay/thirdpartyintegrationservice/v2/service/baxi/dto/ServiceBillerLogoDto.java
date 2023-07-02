package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceBillerLogoDto {
    private long id;
    private String serviceCategory;
    private String serviceBiller;
    private long biller_id;
    private String serviceType;
    private String serviceName;
    private String serviceEnabled;
    private String serviceStatus;
    private String serviceLogo;
    private String deployed;
    private String b2b_deployed;
}
