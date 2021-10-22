package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.Data;

@Data
public class PaymentTransactionDetailDto {
    private String transactionId;
    private Double amount;

}
