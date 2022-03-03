package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferFromOfficialToMainWallet {
    private BigDecimal amount;
    private String customerCreditAccount;
    private String officeDebitAccount;
    private String paymentReference;
    private String tranCrncy;
    private String tranType;
    private String tranNarration;
    private String billsPaymentTransactionId;
    private String userId;
    private String transactionCategory;

}
