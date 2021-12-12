package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Optional;

public enum CommissionType {

    FIXED,
    PERCENTAGE;

    public static Optional<CommissionType> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(CommissionType.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
