package com.wayapay.thirdpartyintegrationservice.v2.dto;

import java.util.Optional;

public enum PaymentStatus {
    CANCELLED, FAILED, SUCCESSFUL, ERROR, ABANDONED, EXPIRED, PENDING,
    APPROVED, REJECTED, DECLINED, INITIATED, TIMEOUT,REFUNDED;

    public static Optional<PaymentStatus> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(PaymentStatus.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static boolean isNonEmpty(String value){
        return value != null && !value.isEmpty();
    }
}
