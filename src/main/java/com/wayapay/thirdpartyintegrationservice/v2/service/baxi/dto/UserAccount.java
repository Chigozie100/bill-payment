package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import lombok.Data;

@Data
public class UserAccount {
    private String name;
    private String accountNumber;
    private String minimumAmount;
}
