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
public class QuickTellerConfig {

    @NotBlank(message = "app.config.quickteller.base-url is required")
    private String baseUrl;

    @NotBlank(message = "app.config.quickteller.client-id is required")
    private String clientId;

    @NotBlank(message = "app.config.quickteller.terminal-id is required")
    private String terminalId;

    @NotBlank(message = "app.config.quickteller.secret is required")
    private String secret;

    @NotBlank(message = "app.config.quickteller.account-number is required")
    private String accountNumber;

    @NotBlank(message = "app.config.quickteller.biller-category-url is required")
    private String billerCategoryUrl;

    @NotBlank(message = "app.config.quickteller.billers-url is required")
    private String billersUrl;

    @NotBlank(message = "app.config.quickteller.biller-payment-item-url is required")
    private String billerPaymentItemUrl;

    @NotBlank(message = "app.config.quickteller.customer-validation-url is required")
    private String customerValidationUrl;

    @NotBlank(message = "app.config.quickteller.send-payment-advice-url is required")
    private String sendPaymentAdviceUrl;

    @NotBlank(message = "app.config.quickteller.transaction-ref-code is required")
    private String transactionRefCode;

    private String ussdUrl;
}
