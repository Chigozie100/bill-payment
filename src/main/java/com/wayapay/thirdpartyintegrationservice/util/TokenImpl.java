package com.wayapay.thirdpartyintegrationservice.util;


import java.util.HashMap;
import java.util.Map;

import com.wayapay.thirdpartyintegrationservice.v2.dto.request.LoginDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.TokenCheckResponse;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.AuthProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenImpl {

    @Autowired
    private AuthProxy authProxy;

    @Autowired
    private Environment environment;

    public String getToken() throws ThirdPartyIntegrationException {

//        Map<String, String> map = new HashMap<>();
//        map.put("emailOrPhoneNumber",  environment.getProperty("waya.service.username"));
//        map.put("password", environment.getProperty("waya.service.password"));
//        map.put("otp", "");
        try {
            LoginDto loginDto = new LoginDto();
            loginDto.setOtp("");
            loginDto.setPassword(environment.getProperty("waya.service.password"));
            loginDto.setEmailOrPhoneNumber( environment.getProperty("waya.service.username"));
            TokenCheckResponse tokenData = authProxy.getToken(loginDto);
            return tokenData.getData().getToken();
        }catch (Exception ex){
            log.error("::Error Login {}",ex.getLocalizedMessage());
            return "";
        }
    }

    public String getPin() throws ThirdPartyIntegrationException {

       return environment.getProperty("waya.service.pin");
    }

}
