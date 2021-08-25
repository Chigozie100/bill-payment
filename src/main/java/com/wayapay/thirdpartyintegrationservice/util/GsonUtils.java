package com.wayapay.thirdpartyintegrationservice.util;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wayapay.thirdpartyintegrationservice.dto.MainWalletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import static com.wayapay.thirdpartyintegrationservice.util.Constants.DATE_FORMAT;

@Slf4j
public class GsonUtils {

    private static ThreadLocal<GsonBuilder> gsonBuilder = new ThreadLocal<>();
    private static ThreadLocal<Boolean> transientMode = ThreadLocal.withInitial(() -> true);

    private static Gson getGson(){
        if(gsonBuilder.get() != null) {
            if(!transientMode.get()) return gsonBuilder.get().create();

            Gson gson = gsonBuilder.get().create();
            gsonBuilder.remove();
            return gson;
        }

        return getDefaultGsonBuilder().create();
    }

    public static void setTransientGsonBuilder(GsonBuilder gsonBuilderObject){
        gsonBuilder.set(gsonBuilderObject);
        transientMode.set(true);
    }

    public static void setGsonBuilder(GsonBuilder gsonBuilderObject){
        gsonBuilder.set(gsonBuilderObject);
        transientMode.set(false);
    }

    public static void resetGsonBuilder(){
        gsonBuilder.remove();
        transientMode.set(true);
    }

    public static GsonBuilder getDefaultGsonBuilder(){

        return new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .serializeNulls()
                .setFieldNamingStrategy(field -> {
                    for(Annotation annotation : field.getAnnotations()){
                        if(annotation instanceof JsonProperty) {
                            return ((JsonProperty) annotation).value();
                        }
                    }
                    return field.getName();
                })
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        for(Annotation annotation : fieldAttributes.getAnnotations()){
                            if(annotation instanceof JsonIgnore || annotation instanceof JsonBackReference) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass) {

                        return false;
                    }
                })
                .registerTypeHierarchyAdapter(PersistableEnum.class, new GsonGenericEnumDeserializer<>())
                .registerTypeHierarchyAdapter(PersistableEnum.class, new GsonGenericEnumSerializer<>());
    }

    public static Collection<Object> castToCollection(String json){
        Type type = TypeToken.getParameterized(ArrayList.class, Object.class).getType();
        return getGson().fromJson(json, type);
    }

    public static <T> Collection<T> castToCollection(String json, Class<T> t){
        Type type = TypeToken.getParameterized(ArrayList.class, t).getType();
        return getGson().fromJson(json, type);
    }

    public static <T> Collection<T> castToCollection(String json, Type t){
        Type type = TypeToken.getParameterized(ArrayList.class, t).getType();
        return getGson().fromJson(json, type);
    }

    public static Map<String,Object> castToMap(String json){
        Type type = TypeToken.getParameterized(LinkedHashMap.class, String.class, Object.class).getType();
        return getGson().fromJson(json, type);
    }

    public static <T> Map<String,T> castToMap(String json, Class<T> t){
        Type type = TypeToken.getParameterized(LinkedHashMap.class, String.class, t).getType();
        return getGson().fromJson(json, type);
    }

    public static <K,V> Map<K,V> castToMap(String json, Class<K> k, Type v){
        Type type = TypeToken.getParameterized(LinkedHashMap.class, k, v).getType();
        return getGson().fromJson(json, type);
    }

    public static Optional<String> objectToJson(Object object){
        try {
            return Optional.of(new ObjectMapper().writeValueAsString(object));
        } catch (JsonProcessingException e) {
            log.error("Unable to generate json equivalent of the provided object");
            return Optional.empty();
        }
    }
    public static ObjectMapper getObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public static Object cast(String json){
        return getGson().fromJson(json, Object.class);
    }

    public static <T> T cast(String json, Class<T> type){
        return getGson().fromJson(json, type);
    }

    public static <T> T cast(String json, Type type){
        return getGson().fromJson(json, type);
    }

    public static <T> T  castFromObject(Object obj, Class<T> type) {
        return cast(getGson().toJson(obj), type);
    }

    public static <T> T  castFromObject(Object obj, Type type) {
        return cast(getGson().toJson(obj), type);
    }

    public static String castToJson(Object object) {
        return getGson().toJson(object);
    }



    public void unload() {
        transientMode.remove(); // Compliant
    }

}