package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SuperResponse {

    private String status;
    private String message;
    private String code;

}