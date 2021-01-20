package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthApiTokenRequest {

    private String wallet;
    private String username;
    private String password;
    private String identifier;

}
