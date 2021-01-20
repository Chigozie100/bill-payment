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
public class BaxiConfig {

    @NotBlank(message = "app.config.baxi.base-url is required")
    private String baseUrl;

    @NotBlank(message = "app.config.baxi.x-api-key is required")
    private String xApiKey;
}
