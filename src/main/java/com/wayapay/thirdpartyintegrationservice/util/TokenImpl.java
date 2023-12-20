package com.wayapay.thirdpartyintegrationservice.util;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.wayapay.thirdpartyintegrationservice.config.AppCache;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.LoginDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.TokenCheckResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.UserDto;
import com.wayapay.thirdpartyintegrationservice.v2.proxyclient.AuthProxy;
import feign.FeignException;
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


    public String getToken() {

        try {
            if(AppCache.adminExpireDate != null){
                boolean tokenExpire = checkIfTokenHasExpired(AppCache.adminExpireDate);
                if(!tokenExpire)
                    return AppCache.adminToken;
            }
            UserDto tokenData = getTokenData();
            if(tokenData != null){
                AppCache.adminToken = tokenData.getToken();
                AppCache.adminExpireDate = LocalDateTime.now();
                return AppCache.adminToken;
            }
            return null;
        } catch (Exception ex) {
            log.error("::ERROR FETCHING ADMIN ACCESS TOKEN {}", ex);
            return null;
        }
    }


    private UserDto getTokenData() {

        try {
            LoginDto loginDto = new LoginDto();
            loginDto.setOtp("");
            loginDto.setPassword(environment.getProperty("waya.service.password"));
            loginDto.setEmailOrPhoneNumber( environment.getProperty("waya.service.username"));
            String clientId = "WAYABANK";
            String clientType = "ADMIN";
            TokenCheckResponse tokenData = authProxy.getToken(loginDto,clientId,clientType);
            return tokenData.getData();
        }catch (FeignException ex){
            log.error("::Error Login {}, msg {}",ex.getLocalizedMessage(),ex.contentUTF8());
            ex.printStackTrace();
            return null;
        }
    }

    public String getPin() {

       return environment.getProperty("waya.service.pin");
    }

    private boolean checkIfTokenHasExpired(LocalDateTime tokenTime){
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime dateAfter59Minutes = tokenTime.plusHours(5);

            if (currentTime.isAfter(dateAfter59Minutes)) {
                log.info("currentTime is ahead of tokenTime by 5 hrs or more.");
                return true;
            } else {
                log.info("currentTime is not ahead of tokenTime by 5 hrs.");
                return false;
            }
        }catch (Exception ex){
            log.error("::Error checkIfTokenHasExpired {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return true;
        }
    }

}
