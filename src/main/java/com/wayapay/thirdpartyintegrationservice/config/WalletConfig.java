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
public class WalletConfig {

    @NotBlank(message = "app.config.wallet.base-url is required")
    private String baseUrl;

}
