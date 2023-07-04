package com.wayapay.thirdpartyintegrationservice.v2.service.notification;

import lombok.Data;

@Data
public class PaymentTransactionDetailDto {
    private String transactionId;
    private Double amount;

}
