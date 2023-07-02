package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateChargeDto {
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal fees = BigDecimal.ZERO;
    private BigDecimal billerCharges = BigDecimal.ZERO;
    private BigDecimal consumerCharges = BigDecimal.ZERO;
}
