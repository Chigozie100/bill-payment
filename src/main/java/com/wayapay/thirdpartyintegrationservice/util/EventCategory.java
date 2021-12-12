package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Optional;

public enum EventCategory {
    WELCOME,
    DISPUTE,
    TRANSACTION,
    INVITE_TEAM;

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