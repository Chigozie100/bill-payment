package com.wayapay.thirdpartyintegrationservice.dto;


import lombok.Data;

@Data
public class ProfileDto {

    private String id;
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
    private OtherDetailsDto otherDetails;
}
