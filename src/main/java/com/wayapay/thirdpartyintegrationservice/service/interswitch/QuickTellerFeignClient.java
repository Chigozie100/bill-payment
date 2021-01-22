package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import static com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerConstants.*;

@FeignClient(name = "quickteller-feign-client", url = "${app.config.quickteller.base-url}")
public interface QuickTellerFeignClient {

    @PostMapping(value = "/passport/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AuthResponse getAuthToken(@RequestHeader String authorization);

    @GetMapping("${app.config.quickteller.biller-category-url}")
    CategoryResponse getCategory(@RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

    @GetMapping("${app.config.quickteller.billers-url}")
    GetAllBillersResponse getAllBillers(@RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

    @GetMapping("${app.config.quickteller.biller-payment-item-url}")
    GetBillerPaymentItemResponse getBillerPaymentItems(@PathVariable("billerId") String billerId, @RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);


}
