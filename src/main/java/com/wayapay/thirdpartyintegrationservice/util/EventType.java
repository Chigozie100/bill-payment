package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Optional;

public enum EventType {
    SMS,
    EMAIL,
    IN_APP;
    public static Optional<EventType> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(EventType.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
