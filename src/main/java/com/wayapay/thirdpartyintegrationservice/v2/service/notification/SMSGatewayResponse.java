package com.wayapay.thirdpartyintegrationservice.v2.service.notification;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
public class SMSGatewayResponse {
    private Long id;
    private boolean active;
    private String name;
    private String description;
}

