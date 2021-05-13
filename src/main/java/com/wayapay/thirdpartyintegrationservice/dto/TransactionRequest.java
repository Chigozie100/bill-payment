package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
	private Long customerWalletId;
    private String paymentReference;
    private String description;
    private BigDecimal amount;
}
