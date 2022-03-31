package com.wayapay.thirdpartyintegrationservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class CommonUtils {

    private CommonUtils() {
    }

    public static boolean isEmpty(String value){
        return Objects.isNull(value) || value.isEmpty();
    }

    public static boolean isNonEmpty(String value){
        return value != null && !value.isEmpty();
    }

    public static Optional<String> objectToJson(Object object){
        try {
            return Optional.of(new ObjectMapper().writeValueAsString(object));
        } catch (JsonProcessingException e) {
            log.error("Unable to generate json equivalent of the provided object");
            return Optional.empty();
        }
    }

    public static Date convertToDate(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static String getDateAsString(LocalDateTime localDateTime){
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    }

    public static long generatePaymentTransactionId() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Africa/Lagos"));
        return calendar.getTimeInMillis() / 1000L;
    }


    public static ObjectMapper getObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public static long validateAndFetchIdAsLong(String id) throws ThirdPartyIntegrationException {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            log.error("Invalid Id provided => {}", id, e);
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.ID_IS_INVALID);
        }
    }
}
