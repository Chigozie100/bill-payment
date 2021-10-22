package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Optional;

public enum SMSEventStatus {
    TRANSACTION,
    MESSAGING,
    BILLSPAYMENT,
    ADVERT;

    public static Optional<SMSEventStatus> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(SMSEventStatus.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
