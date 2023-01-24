package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.service.baxi.BettingUser;
import lombok.Data;

@Data
public class BettingValidationResponse {
    private String status;
    private String message;
    private int code;
    private BettingUser data;
}