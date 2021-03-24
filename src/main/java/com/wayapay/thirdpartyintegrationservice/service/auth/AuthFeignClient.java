package com.wayapay.thirdpartyintegrationservice.service.auth;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-feign-client", url = "${app.config.auth.base-url}")
public interface AuthFeignClient {

    @PostMapping("/auth/validate-user")
    AuthResponse userTokenValidation(@RequestHeader String authorization);

}
