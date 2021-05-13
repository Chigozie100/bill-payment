package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferFromWalletPojo {

	  private Long customerWalletId;
	  private String paymentReference;
	  private Double amount;
}
