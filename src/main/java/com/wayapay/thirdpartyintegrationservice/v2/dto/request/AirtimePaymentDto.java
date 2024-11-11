package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class AirtimePaymentDto {
    @NotNull(message = "Amount cannot be null")
    @Min(value = 1, message = "Amount must be greater than 0")
    private int amount;
    private String plan;
    @NotNull(message = "Phone number cannot be null")
    @Size(min = 11, max = 11, message = "Phone number must be exactly 11 digits")
    @Pattern(regexp = "\\d{11}", message = "Phone number must contain only digits")
    private String phone;
    private String email;
    private String type;
}
