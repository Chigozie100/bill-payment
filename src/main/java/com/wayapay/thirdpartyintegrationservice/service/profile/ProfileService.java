//package com.wayapay.thirdpartyintegrationservice.service.profile;
//
//import com.google.gson.JsonSyntaxException;
//import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
//import com.wayapay.thirdpartyintegrationservice.util.GsonUtils;
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.configurationprocessor.json.JSONException;
//import org.springframework.boot.configurationprocessor.json.JSONObject;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//
//@Data
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class ProfileService {
//
//    private final ProfileFeignClient profileFeignClient;
//
//    public UserProfileResponse getUserProfile(String userId, String token) throws ThirdPartyIntegrationException {
//        log.info("inside getUserProfile ::::: {} ::::: " +userId);
//        String user;
//        ResponseEntity<String> response = null;
//        try {
//            //response =  profileFeignClient.getUserProfile(userId,token);
//            log.info("inside getUserProfile  2::::: {} ::::: " +response);
//            if (response.getStatusCode().isError()) {
//                throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, response.getStatusCode().toString());
//            }
//            user = response.getBody();
//            JSONObject jsonpObject = new JSONObject(user);
//            String json = jsonpObject.getJSONObject("data").toString();
//            UserProfileResponse notificationDto1 = GsonUtils.cast(json, UserProfileResponse.class);
//            log.info("Back from Profile Service ::: " + notificationDto1);
//            return notificationDto1;
//
//        } catch (RestClientException | JsonSyntaxException | ThirdPartyIntegrationException | JSONException e) {
//            System.out.println("Error is here " + e.getMessage());
//            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, e.getMessage());
//        }
//
//    }
//}
