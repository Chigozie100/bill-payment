package com.wayapay.thirdpartyintegrationservice.service.profile;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
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
