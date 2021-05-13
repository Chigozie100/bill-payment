package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.Setter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize 
public class MainWalletResponse {

	private Long id;
    private String accountNo;
    private Integer clientId;
    private String clientName;
    private Long savingsProductId;
    private String savingsProductName;
    private Long fieldOfficerId;
    private Double nominalAnnualInterestRate;
    private WalletStatus status;
    private WalletTimeLine timeline;
    private WalletCurrency currency;
    private WalletSummary summary;
}
