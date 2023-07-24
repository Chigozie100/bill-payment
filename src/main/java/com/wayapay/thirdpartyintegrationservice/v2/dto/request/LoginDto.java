package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.Data;

@Data
public class LoginDto {
    private String emailOrPhoneNumber;
    private String otp;
    private String password;
}
