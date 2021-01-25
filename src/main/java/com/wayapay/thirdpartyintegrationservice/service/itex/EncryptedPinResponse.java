package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class EncryptedPinResponse extends SuperResponse {
    private EncryptedPin data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class EncryptedPin {
    private String responseCode;
    private String error;
    private String message;
    private String pin;
}