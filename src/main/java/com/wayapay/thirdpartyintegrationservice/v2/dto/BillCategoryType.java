package com.wayapay.thirdpartyintegrationservice.v2.dto;

import java.util.Optional;

public enum BillCategoryType {
    AIRTIME_TOPUP,
    DATA_TOPUP,
    CABLE,
    UTILITY,
    BETTING;

    public static Optional<BillCategoryType> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(BillCategoryType.valueOf(value.toUpperCase()));
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
