package com.wayapay.thirdpartyintegrationservice.v2.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;

@Data
public class CustomObject {
    private HashMap<String, BigDecimal> map;
}
