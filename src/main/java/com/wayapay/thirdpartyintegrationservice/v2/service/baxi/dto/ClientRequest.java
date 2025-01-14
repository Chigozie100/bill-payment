package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import lombok.Data;

@Data
public class ClientRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String gender;
    private String companyName;
    private String dateOfBirth;
    private String occupation;

}
