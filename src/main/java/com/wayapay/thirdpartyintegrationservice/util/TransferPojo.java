package com.wayapay.thirdpartyintegrationservice.util;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferPojo {
    private BigDecimal amount;
    private String eventId;
    private String customerAccountNumber;
    private String tranNarration;
    private String paymentReference;
    private String tranCrncy;
    private String token;
}
