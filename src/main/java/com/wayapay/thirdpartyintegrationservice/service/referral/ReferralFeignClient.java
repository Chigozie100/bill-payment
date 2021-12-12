package com.wayapay.thirdpartyintegrationservice.service.referral;

import com.wayapay.thirdpartyintegrationservice.dto.ApiResponseBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "REFERRAL-CLIENT", url = "${app.config.referral.base-url}")
public interface ReferralFeignClient {
    @GetMapping("/api/v1/referralcode/get-details/{referralCode}")
    ResponseEntity<ApiResponseBody<ReferralCodePojo>> getUserByReferralCode(@PathVariable String referralCode, @RequestHeader("Authorization") String token);

}
