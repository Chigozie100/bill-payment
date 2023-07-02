package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor @Data
public class TransferFromWalletPojo {
	private BigDecimal amount;
	private String customerAccountNumber;
	private String eventId;
	private String paymentReference;
	private String tranCrncy;
	private String tranNarration;
	private String transactionCategory;
	private Long userId;
	private String senderName;
	private String receiverName;
}
