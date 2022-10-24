package com.wayapay.thirdpartyintegrationservice.service.baxi;

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
    private String smartcardNumber;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("total_amount")
    private String totalAmount;

    @JsonProperty("product_monthsPaidFor")
    private String productMonthsPaidFor;

    @JsonProperty("service_type")
    private String serviceType;
    private String agentId;
    private String phone;
    private String agentReference;

}
