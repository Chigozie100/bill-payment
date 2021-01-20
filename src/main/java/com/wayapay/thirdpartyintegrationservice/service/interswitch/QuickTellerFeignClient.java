package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "quickteller-feign-client", url = "${app.config.quickteller.base-url}")
public interface QuickTellerFeignClient {

    String AUTHORIZATION = "Authorization";
    String SIGNATURE = "Signature";
    String NONCE = "Nonce";
    String TIMESTAMP = "Timestamp";
    String SIGNATURE_METHOD = "SignatureMethod";

    @PostMapping(value = "/passport/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AuthResponse getAuthToken(@RequestHeader String authorization);

    @GetMapping("${app.config.quickteller.biller-category-url}")
    CategoryResponse getCategory(@RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

    @GetMapping("${app.config.quickteller.biller-by-category-url}")
    GetAllBillersByCategoryResponse getAllBillersByCategory(@PathVariable("id") String id, @RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

}
