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
public class KafkaConfig {

    @NotBlank(message = "app.config.kafka.transaction-topic is required")
    private String transactionTopic;

    @NotBlank(message = "app.config.kafka.sell-bills-topic is required")
    private String sellBillsTopic;
}
