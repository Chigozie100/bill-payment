package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Optional;

public enum UserType {

ROLE_USER,
ROLE_CORP,
ROLE_CORP_ADMIN,
ROLE_APP_ADMIN,
ROLE_SUPER_ADMIN,
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
