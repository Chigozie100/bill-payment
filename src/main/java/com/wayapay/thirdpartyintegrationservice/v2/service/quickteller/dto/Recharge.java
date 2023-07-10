package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class Recharge {
    private String biller;
    private String customerId1;
    private String customerId2;
    private String paymentTypeName;
    private String paymentTypeCode;
    private String billerId;

}
