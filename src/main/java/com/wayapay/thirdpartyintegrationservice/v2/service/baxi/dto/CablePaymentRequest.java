package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CablePaymentRequest {
    @JsonProperty("smartcard_number")
    private String smartCardNumber;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("total_amount")
    private int totalAmount;

    @JsonProperty("product_monthsPaidFor")
    private String productMonthsPaidFor;

    @JsonProperty("service_type")
    private String serviceType;
    private String agentId;
    private String phone;
    private String agentReference;

}

