package com.wayapay.thirdpartyintegrationservice.util;


import java.util.Optional;

public enum ProductType {
    WAYAGRAM,
    WAYAPOS,
    WAYAPAY,
    WAYABANK;
    public static Optional<EventCategory> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(EventCategory.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}