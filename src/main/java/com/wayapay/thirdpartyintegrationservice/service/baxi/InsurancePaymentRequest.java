package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InsurancePaymentRequest {

    private ClientRequest client;
    private VehicleRequest vehicle;
    private VehicleOrderRequest order;
    @JsonProperty("service_type")
    private String serviceType;
    private String agentReference;

}
