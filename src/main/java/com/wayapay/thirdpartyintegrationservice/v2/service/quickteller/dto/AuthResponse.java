package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AuthResponse {
    private String access_token;
    private String token_type;
    private String expires_in;
    private String scope;
    private String merchant_code;
    private String requestor_id;
    private String client_name;
    private String payable_id;
    private String jti;
    private String code;
    private String description;
    private String errors;
}
