package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class CableTvPaymentDto {
    @NotNull(message = "service type must not be null")
    private String type;
    private String phone;
    @NotNull(message = "amount must not be null")
    private String amount;
    private String email;
    private String productCode;
    private String smartCardNumber;
    private String monthPaidFor;
}
