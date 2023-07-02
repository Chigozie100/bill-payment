package com.wayapay.thirdpartyintegrationservice.v2.service.notification;

import lombok.Data;

import java.util.List;

@Data
public class SMSDto {
    private String username;
    private String message;
    private String email;
    private String telephone;
    private String eventType;
    private String initiator;

}
