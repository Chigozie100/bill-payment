package com.wayapay.thirdpartyintegrationservice.util;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDto {

    private Long id;
    private String email;
    private String phoneNumber;
    private String referenceCode;
    private String firstName;
    private String surname;
    private String password;
    private boolean phoneVerified;
    private boolean emailVerified;
    private boolean pinCreated;
    private boolean corporate;
    private List<String> roles;
    private List<String> permits;
    private String transactionLimit;
    private String userLocation;
    private String token;
    public UserDto(String email) {
        this.email = email;
    }

}
