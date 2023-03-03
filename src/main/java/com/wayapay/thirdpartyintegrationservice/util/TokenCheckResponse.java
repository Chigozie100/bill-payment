package com.wayapay.thirdpartyintegrationservice.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenCheckResponse {

    private Date timeStamp;
    private boolean status;
    private String message;
    private UserDto data;
}