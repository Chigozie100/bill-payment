package com.wayapay.thirdpartyintegrationservice.v2.service.notification;

import lombok.Data;

import java.util.List;

@Data
public class EmailDto {
    private String message;
    private String email;
    private String fullName;
    private String eventType;
    private String initiator;

}
