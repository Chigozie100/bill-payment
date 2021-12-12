package com.wayapay.thirdpartyintegrationservice.util;

public interface PersistableEnum<T> {
    T getValue();

    default String toJson(){
        return getValue().toString();
    }
}
