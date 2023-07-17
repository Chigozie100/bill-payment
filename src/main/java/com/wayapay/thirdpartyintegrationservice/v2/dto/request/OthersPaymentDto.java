package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class OthersPaymentDto {
    private BigDecimal amount;
    private String type;
    private String email;
    private String phone;
    private String accountNumber;
}
