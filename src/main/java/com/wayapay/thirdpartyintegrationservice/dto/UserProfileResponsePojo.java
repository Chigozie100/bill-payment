package com.wayapay.thirdpartyintegrationservice.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponsePojo{

    @JsonProperty("userId")
    private Long id;
    @Builder.Default
    private String email = "";
    @Builder.Default
    private boolean isEmailVerified = false;
    @Builder.Default
    private String phoneNumber = "";
    @Builder.Default
    private String firstName = "";
    @Builder.Default
    private String lastName = "";
    private boolean isAdmin;
    private boolean isPhoneVerified;
    private boolean isAccountLocked;
    private boolean isAccountExpired;
    private boolean isCredentialsExpired;
    private boolean isActive;
    private boolean isAccountDeleted;
    @Builder.Default
    private String referenceCode = "";
    private boolean pinCreated;
    private boolean isCorporate;
    @Builder.Default
    private String gender = "";
    @Builder.Default
    private String middleName = "";
    @Builder.Default
    private String dateOfBirth = "";
    @Builder.Default
    private String profileImage = "";
    @Builder.Default
    private String district = "";
    @Builder.Default
    private String address = "";
    @Builder.Default
    private String city = "";
    @Builder.Default
    private String state = "";

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private Set<String> permits = new HashSet<>();
}

