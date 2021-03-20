package com.wayapay.thirdpartyintegrationservice.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "app.config")
@Validated
public class AppConfig {

    private ItexConfig itex;
    private BaxiConfig baxi;
    private QuickTellerConfig quickteller;
    private DisputeServiceConfig dispute;
    private WalletConfig wallet;
    private KafkaConfig kafka;

}
