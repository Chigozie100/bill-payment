package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.Data;

@Data
public class TransactionCount {
    private String userId;
    private String transactionId;
    private Object transRequest;
    private Object transResponse;
}
