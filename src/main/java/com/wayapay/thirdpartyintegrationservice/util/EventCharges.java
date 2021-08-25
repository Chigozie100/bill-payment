package com.wayapay.thirdpartyintegrationservice.util;

import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;

import java.util.Optional;

public enum EventCharges {
    SMSCHG,
    AITCOL,
    COMPAYM,
    COMMPMT;
    public static Optional<EventCharges> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(EventCharges.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}