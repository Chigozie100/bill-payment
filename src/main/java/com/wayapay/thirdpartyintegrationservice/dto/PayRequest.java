package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
public class PayRequest {
    private Long customerWalletId;
    private BigDecimal amount;

}
