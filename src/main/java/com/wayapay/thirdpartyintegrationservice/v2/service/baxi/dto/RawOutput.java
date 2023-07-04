package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RawOutput {
    private String accountStatus;
    private String firstName;
    private String lastName;
    private String customerType;
    private String invoicePeriod;
    private String dueDate;
    private String customerNumber;
}
