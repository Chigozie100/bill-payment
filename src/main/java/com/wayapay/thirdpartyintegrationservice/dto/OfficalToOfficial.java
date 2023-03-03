package com.wayapay.thirdpartyintegrationservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OfficalToOfficial {
    private BigDecimal amount;
    private String officeCreditAccount;
    private String officeDebitAccount;
    private String paymentReference;
    private String tranCrncy;
    private String tranType;
    private String tranNarration;  

    public OfficalToOfficial( BigDecimal amount,
    String officeCreditAccount,
    String officeDebitAccount,
    String paymentReference,
    String tranCrncy,
    String tranType,
    String tranNarration) {
        this.officeCreditAccount = officeCreditAccount;
        this.officeDebitAccount= officeDebitAccount;
        this.paymentReference= paymentReference;
        this.tranCrncy= tranCrncy;
        this.tranType= tranType;
        this.tranNarration=tranNarration;

    }


    public OfficalToOfficial() {
    }

}
