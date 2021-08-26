package com.wayapay.thirdpartyintegrationservice.service.profile;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "profile-feign-client", url = "${app.config.auth.base-url}")
public interface ProfileFeignClient {

    @GetMapping("/api/v1/profile/user-referrals/{userId}")
    List<UserProfileResponse> findAllUserReferral(@PathVariable("userId") String userId, String page);

    @GetMapping("/api/v1/profile/{userId}")
    UserProfileResponse getUserProfile(@PathVariable("userId") String userId,  @RequestHeader("Authorization") String token);
}