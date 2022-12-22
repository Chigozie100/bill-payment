package com.wayapay.thirdpartyintegrationservice.util;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.auth.AuthFeignClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenImpl {

    @Autowired
    private AuthFeignClient authProxy;

    @Autowired
    private Environment environment;

    public String getToken() throws ThirdPartyIntegrationException {

        Map<String, String> map = new HashMap<>();
        map.put("emailOrPhoneNumber",  environment.getProperty("waya.service.username"));
        map.put("password", environment.getProperty("waya.service.password"));

        TokenCheckResponse tokenData = authProxy.getToken(map);

        return tokenData.getData().getToken();
    }

}
