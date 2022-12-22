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
 
}
