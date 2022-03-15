package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Optional;

public enum TransactionCategory {

    CABLE,
    UTILITY,
    AIRTIME_TOPUP,
    DATA_TOPUP,
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
