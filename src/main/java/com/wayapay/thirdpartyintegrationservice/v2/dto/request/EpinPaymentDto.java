package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class EpinPaymentDto {
    private int numberOfPins;
    private int amount;
    private int fixAmount;
    private String type;
    private String email;
    private String phone;
}
