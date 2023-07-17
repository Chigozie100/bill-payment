package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElectricityVerificationDetail {
    private String name;
    private String address;
    private String outstandingBalance;
    private String dueDate;
    private String district;
    private String accountNumber;
    private double minimumAmount;
    private RawOutPutResponse rawOutput;
    private String errorMessage;
}
