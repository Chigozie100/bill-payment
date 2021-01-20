package com.wayapay.thirdpartyintegrationservice.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@Validated
public class ItexConfig {

    @NotBlank(message = "app.config.itex.base-url is required")
    private String baseUrl;

    @NotBlank(message = "app.config.itex.wallet-id is required")
    private String walletId;

    @NotBlank(message = "app.config.itex.username is required")
    private String username;

    @NotBlank(message = "app.config.itex.password is required")
    private String password;

    @NotBlank(message = "app.config.itex.unique-api-identifier is required")
    private String uniqueApiIdentifier;

    @NotBlank(message = "app.config.itex.pay-vice-pin is required")
    private String payVicePin;

    @NotBlank(message = "app.config.itex.hmacsha256key is required")
    private String hmacsha256key;
}
