package com.wayapay.thirdpartyintegrationservice.service.notification;

import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseObj;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "notification-feign-client", url = "${app.config.notification.base-url}")
public interface NotificationFeignClient {

    @PostMapping("/in-app-notification")
    ResponseEntity<ResponseObj> inAppNotifyUser(@RequestBody InAppEvent inAppEvent, @RequestHeader("Authorization") String token);

    @PostMapping("/email-notification")
    ResponseEntity<ResponseObj> emailNotifyUser(@RequestBody EmailEvent emailDto, @RequestHeader("Authorization") String token);

    @PostMapping("/sms-notification-atalking")
    ResponseEntity<ResponseObj> smsNotifyUserAtalking(@RequestBody SMSDto smsDto, @RequestHeader("Authorization") String token);

    @PostMapping("/sms-notification-infobip")
    ResponseEntity<ResponseObj> smsNotifyUserInfobip(@RequestBody SMSDto smsDto, @RequestHeader("Authorization") String token);

    @PostMapping("/sms-notification-twilio")
    ResponseEntity<ResponseObj> smsNotifyUserTwilio(@RequestBody SMSDto smsDto, @RequestHeader("Authorization") String token);

    @GetMapping("/admin/view-active-sms-charge")
    ResponseEntity<String> getActiveSMSCharge(@RequestHeader("Authorization") String token);
}
