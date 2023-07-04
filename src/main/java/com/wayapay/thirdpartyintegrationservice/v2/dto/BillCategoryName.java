package com.wayapay.thirdpartyintegrationservice.v2.dto;

import java.util.Optional;

public enum BillCategoryName {
    airtime , databundle , cabletv , epin , betting ,electricity,education,vehicle,insurance;

    public static Optional<BillCategoryName> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(BillCategoryName.valueOf(value.toUpperCase()));
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
