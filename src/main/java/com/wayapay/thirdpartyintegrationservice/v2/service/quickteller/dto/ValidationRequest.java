package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public
class ValidationRequest {
    private String customerId;
    private String paymentCode;
}
