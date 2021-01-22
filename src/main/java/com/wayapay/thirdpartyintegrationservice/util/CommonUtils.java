package com.wayapay.thirdpartyintegrationservice.util;

import java.util.Objects;

public class CommonUtils {

    private CommonUtils() {
    }

    public static boolean isEmpty(String value){
        return Objects.isNull(value) || value.isEmpty();
    }

    public static boolean isNonEmpty(String value){
        return value != null && !value.isEmpty();
    }
}
