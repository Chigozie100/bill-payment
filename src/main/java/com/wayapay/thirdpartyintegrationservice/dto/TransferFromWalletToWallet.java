package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferFromWalletToWallet {
    private BigDecimal amount;
    private String customerAccountNumber;
    private String debitAccountNumber;
    private String paymentReference;
    private String tranCrncy;
    private String tranNarration;

}