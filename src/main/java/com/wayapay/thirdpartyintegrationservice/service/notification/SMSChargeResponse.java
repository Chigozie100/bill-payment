package com.wayapay.thirdpartyintegrationservice.service.notification;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SMSChargeResponse {
    private Long id;
    private BigDecimal fee;
    private boolean active = true;
}
