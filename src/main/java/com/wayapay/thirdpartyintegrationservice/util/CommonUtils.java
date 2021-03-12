package com.wayapay.thirdpartyintegrationservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

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

    public static String generatePaymentTransactionId() throws NoSuchAlgorithmException {
        return new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + generateRandomNumber();
    }

    private static String generateRandomNumber() throws NoSuchAlgorithmException {
        int lengthOfNumbers = 18;
        StringBuilder numbers = new StringBuilder("");

        Random random = SecureRandom.getInstanceStrong();
        for (int i = 0; i < lengthOfNumbers; i++) {
            numbers.append(random.nextInt() * 9);
        }

        return numbers.substring(0, 40);
    }
}
