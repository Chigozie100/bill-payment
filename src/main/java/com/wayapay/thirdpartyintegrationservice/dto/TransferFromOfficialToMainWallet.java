package com.wayapay.thirdpartyintegrationservice.dto;

import java.math.BigDecimal;

public class TransferFromOfficialToMainWallet {
    private BigDecimal amount;
    private String customerCreditAccount;
    private String officeDebitAccount;
    private String paymentReference;
    private String tranCrncy;
    private String tranNarration;
}
