package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Optional;

public enum TransactionType {

    BILLS_PAYMENT,
    TRANSFER;

    public static Optional<TransactionType> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(TransactionType.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}