package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleOrderRequest {
    private String vehicleTypeId;
    private String orderDescriptionId;
    private String orderDescriptionName;
    private String vehicleManufacturerId;
    private String vehicleModelId;
    private String city;
    private BigDecimal amount;
    private String transactionCode;
    private String policyholder;
    private String action;
}
