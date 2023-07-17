package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class NameFinderDetail {
    private String name;
    private String address;
    private String outstandingBalance;
    private String dueDate;
    private String district;
    private String accountNumber;
    private String minimumAmount;
    private RawOutput rawOutput;
    private String errorMessage;
}
