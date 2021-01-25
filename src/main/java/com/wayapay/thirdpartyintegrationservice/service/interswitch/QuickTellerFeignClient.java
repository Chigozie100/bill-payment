package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import static com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerConstants.*;

@FeignClient(name = "quickteller-feign-client", url = "${app.config.quickteller.base-url}")
public interface QuickTellerFeignClient {

    @GetMapping("${app.config.quickteller.biller-category-url}")
    CategoryResponse getCategory(@RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

    @GetMapping("${app.config.quickteller.billers-url}")
    GetAllBillersResponse getAllBillers(@RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

    @GetMapping("${app.config.quickteller.biller-payment-item-url}")
    GetBillerPaymentItemResponse getBillerPaymentItems(@PathVariable("billerId") String billerId, @RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

    @PostMapping("${app.config.quickteller.customer-validation-url}")
    QuickTellerCustomerValidationResponse validateCustomerInfo(@RequestBody QuickTellerCustomerValidationRequest request, @RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod, @RequestHeader(TERMINAL_ID) String terminalId);

    @PostMapping("${app.config.quickteller.customer-validation-url}")
    SendPaymentAdviceResponse sendPaymentAdvice(@RequestBody SendPaymentAdviceRequest request, @RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod, @RequestHeader(TERMINAL_ID) String terminalId);
}
