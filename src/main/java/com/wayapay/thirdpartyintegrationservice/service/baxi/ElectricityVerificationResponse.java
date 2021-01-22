package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ElectricityVerificationResponse extends SuperResponse{
    private ElectricityVerificationDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class ElectricityVerificationDetail{
    private String name;
    private String address;
    private String outstandingBalance;
    private String dueDate;
    private String district;
    private String accountNumber;
    private String minimumAmount;
    private String rawOutput;
    private String errorMessage;
}
