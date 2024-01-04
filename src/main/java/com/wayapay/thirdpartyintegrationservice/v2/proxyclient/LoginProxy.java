package com.wayapay.thirdpartyintegrationservice.v2.proxyclient;

import com.wayapay.thirdpartyintegrationservice.config.ClientConfiguration;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.LoginDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.TokenCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@FeignClient(name = "wallet-feign-client", url = "${app.config.auth.base-url}",configuration = ClientConfiguration.class)
public interface LoginProxy {

    @PostMapping(path = "/api/v1/auth/login")
    TokenCheckResponse getToken(@Valid @RequestBody LoginDto request, @RequestHeader(Constants.CLIENT_ID) String clientId, @RequestHeader(Constants.CLIENT_TYPE) String clientType);
}
