package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class ReceiptRequest {
    private String referenceNumber;
    private Date transactionDate;
    private BigDecimal amount;
    private String transactionType;
    private String receiverName;
    private String receiverAccount;
    private String receiverBank;
    private String userId;
    private String amountInWords;
    private String senderName;
}
