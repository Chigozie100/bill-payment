package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class ElectricityPaymentDto {
    @NotNull(message = "amount cannot be null")
    private BigDecimal amount;
    @NotNull(message = "service type cannot be null")
    private String type;
    private String phone;
    @NotNull(message = "account cannot be null")
    private String account;
    private String email;
}
