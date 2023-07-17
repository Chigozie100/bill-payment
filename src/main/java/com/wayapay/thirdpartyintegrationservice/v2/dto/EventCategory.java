package com.wayapay.thirdpartyintegrationservice.v2.dto;

import java.util.Optional;

public enum EventCategory {

    BILLS_PAYMENT;

    public static Optional<EventCategory> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(EventCategory.valueOf(value.toUpperCase()));
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