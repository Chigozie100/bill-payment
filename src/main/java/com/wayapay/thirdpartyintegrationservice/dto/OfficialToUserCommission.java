package com.wayapay.thirdpartyintegrationservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OfficialToUserCommission {
    private BigDecimal amount;
    private String customerCreditAccount;
    private String officeDebitAccount;
    private String paymentReference;
    private String tranCrncy;
    private String tranType;
    private String tranNarration;  
    private String transactionCategory;

    public OfficialToUserCommission( BigDecimal amount,
    String customerCreditAccount,
    String officeDebitAccount,
    String paymentReference,
    String tranCrncy,
    String tranType,
    String tranNarration, 
    String transactionCategory) {
        this.customerCreditAccount = customerCreditAccount;
        this.officeDebitAccount= officeDebitAccount;
        this.paymentReference= paymentReference;
        this.tranCrncy= tranCrncy;
        this.tranType= tranType;
        this.tranNarration=tranNarration;
        this.transactionCategory=transactionCategory;

    }


    public OfficialToUserCommission() {
    }
}
