package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class Profile {
    private UUID id;
    private String email;
    private String firstName;
    private String surname;
    private String phoneNumber;
    private String organisationName;
    private String middleName;
    private String profileImage;
    private String dateOfBirth;
    private String gender;
    private String district;
    private String address;
    private String city;
    private String state;
    private boolean deleted;
    private String userId;
    private String referral;
    private boolean corporate;
}
