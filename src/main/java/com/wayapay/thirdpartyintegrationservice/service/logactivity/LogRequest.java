package com.wayapay.thirdpartyintegrationservice.service.logactivity;

import lombok.Data;

import java.util.Date;

@Data
public class LogRequest {
    private Long id;
    private String action;
    private String jsonRequest;
    private String jsonResponse;
    private String message;
    private String module;
    private Date requestDate;
    private Date responseDate;
    private Long userId;
}
