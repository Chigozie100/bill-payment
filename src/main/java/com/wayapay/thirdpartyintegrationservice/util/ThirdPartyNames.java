package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Optional;

public enum ThirdPartyNames {

    ITEX,
    BAXI,
    QUICKTELLER;

    public static Optional<ThirdPartyNames> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(ThirdPartyNames.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
