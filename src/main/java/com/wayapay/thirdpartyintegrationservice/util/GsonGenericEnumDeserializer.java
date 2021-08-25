package com.wayapay.thirdpartyintegrationservice.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class GsonGenericEnumDeserializer<T extends Enum<T> & PersistableEnum> implements JsonDeserializer<T> {

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        try {
            Class<T> clazz = (Class<T>) Class.forName(type.getTypeName());

            Object value = jsonElement.getAsString();
            T[] enums = clazz.getEnumConstants();
            if ("java.lang.Integer".equals(clazz.getDeclaredConstructors()[0].getGenericParameterTypes()[0].getTypeName()))
                value = Integer.valueOf(value.toString());

            for (T e : enums) {
                if (e.getValue().equals(value)) {
                    return e;
                }
            }

        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }

        return null;
    }

}