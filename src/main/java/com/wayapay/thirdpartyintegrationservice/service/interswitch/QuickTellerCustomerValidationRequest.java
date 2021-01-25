package com.wayapay.thirdpartyintegrationservice.service.interswitch;

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
