package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class QuickTellerCustomerValidationRequest {
    private List<ValidationRequest> customers = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
class ValidationRequest {
    private String customerId;
    private String paymentCode;
}
