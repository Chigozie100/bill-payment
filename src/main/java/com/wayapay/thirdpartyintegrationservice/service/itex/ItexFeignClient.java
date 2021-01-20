package com.wayapay.thirdpartyintegrationservice.service.itex;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "itex-feign-client", url = "${app.config.itex.base-url}")
public interface ItexFeignClient {

    @PostMapping("/vas/authenticate/me")
    ResponseEntity<Object> getAuthApiToken(@RequestBody AuthApiTokenRequest authApiTokenRequest);

    @PostMapping("/vas/credentials/encrypt-pin")
    ResponseEntity<Object> generateEncryptedPin(@RequestBody EncryptedPinRequest encryptedPinRequest);

}
