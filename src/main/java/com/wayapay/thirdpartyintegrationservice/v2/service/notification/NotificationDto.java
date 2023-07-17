package com.wayapay.thirdpartyintegrationservice.v2.service.notification;

import lombok.Data;

@Data
public class NotificationDto {
    private String userId;
    private String message;
    private String type;
    private String eventType;
    private String initiator;

}
