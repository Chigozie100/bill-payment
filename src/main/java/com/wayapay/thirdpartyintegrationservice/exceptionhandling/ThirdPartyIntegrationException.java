package com.wayapay.thirdpartyintegrationservice.exceptionhandling;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ThirdPartyIntegrationException extends Exception {

    private HttpStatus httpStatus;

    public ThirdPartyIntegrationException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
