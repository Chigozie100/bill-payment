package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AuthApiTokenResponse extends SuperResponse {
    private AuthApiToken data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class AuthApiToken {
    private String responseCode;
    private String error;
    private String message;
    private String apiToken;
}
