package com.wayapay.thirdpartyintegrationservice.service.notification;

import lombok.Data;

import java.util.List;
@Data
public class NotificationDetail {
    private String message;
    private String type;
    private List<Users> users;
}

