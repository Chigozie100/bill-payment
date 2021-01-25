package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ElectricityValidationResponse extends SuperResponse{
    private ElectricityValidationDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class ElectricityValidationDetail{
    private String error;
    private String message;
    private String customerId;
    private String name;
    private String meterNumber;
    private String accountNumber;
    private String businessUnit;
    private String businessUnitId;
    private String undertaking;
    private String phone;
    private String address;
    private String email;
    private String lastTransactionDate;
    private String minimumPurchase;
    private String customerArrears;
    private String tariffCode;
    private String tariff;
    private String description;
    private String customerType;
    private String responseCode;
    private String productCode;
}
