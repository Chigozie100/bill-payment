package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class AirtimePaymentDto {
    private int amount;
    private String plan;
    private String phone;
//    private String reference;
    private String type;
}
