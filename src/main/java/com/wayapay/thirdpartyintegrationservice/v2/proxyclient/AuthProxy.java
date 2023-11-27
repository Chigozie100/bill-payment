package com.wayapay.thirdpartyintegrationservice.v2.proxyclient;

import com.wayapay.thirdpartyintegrationservice.config.ClientConfiguration;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.LoginDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.TokenCheckResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponseBody;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.AuthResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.Profile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@FeignClient(name = "wallet-feign-client", url = "${app.config.auth.base-url}",configuration = ClientConfiguration.class)
public interface AuthProxy {

    @PostMapping(path = "/api/v1/auth/validate-user")
    AuthResponse validateUserToken(@RequestHeader String authorization, @RequestHeader(Constants.CLIENT_ID) String clientId, @RequestHeader(Constants.CLIENT_TYPE) String clientType);

    @GetMapping(path = "/api/v1/profile/{userId}")
    ResponseEntity<ApiResponseBody<Profile>> getProfile(@PathVariable String userId, @RequestHeader String authorization, @RequestHeader(Constants.CLIENT_ID) String clientId, @RequestHeader(Constants.CLIENT_TYPE) String clientType);

    @PostMapping(path = "/api/v1/auth/login")
    TokenCheckResponse getToken(@Valid @RequestBody LoginDto request, @RequestHeader(Constants.CLIENT_ID) String clientId, @RequestHeader(Constants.CLIENT_TYPE) String clientType);
}
