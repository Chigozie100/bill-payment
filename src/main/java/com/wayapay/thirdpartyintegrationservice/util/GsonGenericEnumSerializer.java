package com.wayapay.thirdpartyintegrationservice.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class GsonGenericEnumSerializer<T extends Enum<T> & PersistableEnum> implements JsonSerializer<T> {


    @Override
    public JsonElement serialize(T t, Type type, JsonSerializationContext jsonSerializationContext) {

        return jsonSerializationContext.serialize(t.toJson());
    }
}

