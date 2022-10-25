package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Data;

@Data
public class VehicleRequest{
    private String vehicleType;
    private String vehicleManufacturer;
    private String vehicleModel;
    private String registrationNumber;
    private String engineNumber;
    private String chassisNumber;
    private long yearOfManufacture;
    private String color;
    private long yearOfPurchase;
}
