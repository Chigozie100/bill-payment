package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class DataBundlePaymentDto {
    private String productCode;
    private String type;
    private String amount;
    private String phone;
//    private String reference;
}
