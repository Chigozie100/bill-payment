package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ElectricPaymentRequest {
    private String account_number;
    private String amount;
    private String phone;
    private String service_type;
    private String agentId;
    private String metadata;
    private String agentReference;
}
