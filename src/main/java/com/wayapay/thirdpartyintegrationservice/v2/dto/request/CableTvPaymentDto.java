package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class CableTvPaymentDto {
    private String type;
    private String phone;
    private String amount;
    private String email;
    private String productCode;
    private String smartCardNumber;
    private String monthPaidFor;
}
