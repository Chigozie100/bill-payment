package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class NameFinderQueryResponse extends SuperResponse {
    private NameFinderUser data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class NameFinderUser {
    private NameFinderDetail user;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class NameFinderDetail {
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

@Getter
@Setter
@NoArgsConstructor
@ToString
class RawOutput {
    private String accountStatus;
    private String firstName;
    private String lastName;
    private String customerType;
    private String invoicePeriod;
    private String dueDate;
    private String customerNumber;
}