package com.wayapay.thirdpartyintegrationservice.service.itex;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static com.wayapay.thirdpartyintegrationservice.service.itex.ItexConstants.SIGNATURE;
import static com.wayapay.thirdpartyintegrationservice.service.itex.ItexConstants.TOKEN;

@FeignClient(name = "itex-feign-client", url = "${app.config.itex.base-url}")
public interface ItexFeignClient {

    @PostMapping("/vas/authenticate/me")
    AuthApiTokenResponse getAuthApiToken(@RequestBody AuthApiTokenRequest authApiTokenRequest);

    @PostMapping("/vas/credentials/encrypt-pin")
    EncryptedPinResponse generateEncryptedPin(@RequestBody EncryptedPinRequest encryptedPinRequest);

    @PostMapping("/v1/vas/electricity/validation")
    ElectricityValidationResponse electricityValidation(@RequestBody ElectricityValidationRequest electricityValidationRequest, @RequestHeader(TOKEN) String token, @RequestHeader(SIGNATURE) String signature);

    @PostMapping("/v1/vas/data/lookup")
    DataValidationResponse dataValidation(@RequestBody DataValidationRequest dataValidationRequest, @RequestHeader(TOKEN) String token, @RequestHeader(SIGNATURE) String signature);

    @PostMapping("/v1/vas/internet/validation")
    InternetValidationResponse internetValidation(@RequestBody InternetValidationRequest dataValidationRequest, @RequestHeader(TOKEN) String token, @RequestHeader(SIGNATURE) String signature);

    @PostMapping("/v1/vas/internet/bundles")
    InternetBundleResponse getInternetBundles(@RequestBody InternetValidationRequest dataValidationRequest, @RequestHeader(TOKEN) String token, @RequestHeader(SIGNATURE) String signature);

    @PostMapping("/v1/vas/electricity/payment")
    ElectricityPaymentResponse electricityPayment(@RequestBody ElectricityPaymentRequest electricityPaymentRequest, @RequestHeader(TOKEN) String token, @RequestHeader(SIGNATURE) String signature);

    @PostMapping("/v1/vas/vtu/purchase")
    AirtimePaymentResponse airtimePayment(@RequestBody AirtimePaymentRequest airtimePaymentRequest, @RequestHeader(TOKEN) String token, @RequestHeader(SIGNATURE) String signature);

    @PostMapping("/v1/vas/data/subscribe")
    DataPaymentResponse dataPayment(@RequestBody DataPaymentRequest dataPaymentRequest, @RequestHeader(TOKEN) String token, @RequestHeader(SIGNATURE) String signature);

    @PostMapping("/v1/vas/internet/subscription")
    InternetPaymentResponse internetPayment(@RequestBody InternetPaymentRequest internetPaymentRequest, @RequestHeader(TOKEN) String token, @RequestHeader(SIGNATURE) String signature);

}
