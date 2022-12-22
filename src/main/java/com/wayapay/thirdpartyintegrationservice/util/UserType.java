package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Optional;

public enum UserType {
 
ROLE_CORP_ADMIN,
ROLE_USER_MERCHANT,
ROLE_APP_ADMIN,
ROLE_SUPER_ADMIN,
ROLE_MERCHANT,
ROLE_AGGREGATOR,
ROLE_USER_AGENT,
ROLE_USER_AGGREGATOR,
ROLE_OWNER_ADMIN;

    public static Optional<UserType> find(String value){
        if (CommonUtils.isNonEmpty(value)){
            try {
                return Optional.of(UserType.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
