package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class BettingPaymentDto {
    private BigDecimal amount;
    private String type;
//    private String reference;
    private String accountNumber;
}
