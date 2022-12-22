package com.wayapay.thirdpartyintegrationservice.service.auth;

import com.wayapay.thirdpartyintegrationservice.dto.ApiResponseBody;
import com.wayapay.thirdpartyintegrationservice.dto.UserProfileResponsePojo;
import com.wayapay.thirdpartyintegrationservice.service.profile.Profile;
import com.wayapay.thirdpartyintegrationservice.service.referral.ReferralCodePojo;
import com.wayapay.thirdpartyintegrationservice.util.TokenCheckResponse;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-feign-client", url = "${app.config.auth.base-url}")
public interface AuthFeignClient {

    @PostMapping("/api/v1/auth/validate-user")
    AuthResponse userTokenValidation(@RequestHeader String authorization);

    @GetMapping("/api/v1/user/{userId}")  //http://68.183.60.114:8059/api/v1/user/554
    ResponseEntity<ApiResponseBody<UserProfileResponsePojo>> getUserByUserId(@PathVariable String userId, @RequestHeader String authorization);

    @GetMapping("/api/v1/profile/{userId}")
    ResponseEntity<ApiResponseBody<Profile>> getProfile(@PathVariable String userId, @RequestHeader String authorization);


    @PostMapping("/api/v1/auth/login")
    TokenCheckResponse getToken(@RequestBody Map<String, String> request);

    // find user by referrenceCode 
    @GetMapping("/api/v1/referral/get-users-by-referral-code/{referralCode}")
    ResponseEntity<ApiResponseBody<ReferralCodePojo>> getUserByReferralCode(@PathVariable String referralCode, @RequestHeader("Authorization") String token);

}
