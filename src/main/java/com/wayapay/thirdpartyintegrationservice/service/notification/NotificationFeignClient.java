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
    ResponseEntity<ResponseObj<Object>> inAppNotifyUser(@RequestBody InAppEvent inAppEvent, @RequestHeader("Authorization") String token);

    @PostMapping("/email-notification")
    ResponseEntity<ResponseObj<Object>> emailNotifyUser(@RequestBody EmailEvent emailDto, @RequestHeader("Authorization") String token);

    @PostMapping("/api/v1/email-notification-transaction")
    ResponseEntity<ResponseObj<Object>> emailNotifyUserTransaction(@RequestBody EmailEvent emailDto, @RequestHeader("Authorization") String token);


    @PostMapping("/api/v1/sms-notification")
    ResponseEntity<ResponseObj<Object>> smsNotifyUser(@RequestBody SmsEvent smsEvent, @RequestHeader("Authorization") String token);


    @PostMapping("/sms-notification-atalking")
    ResponseEntity<ResponseObj<Object>> smsNotifyUserAtalking(@RequestBody SmsEvent smsEvent, @RequestHeader("Authorization") String token);

    @PostMapping("/sms-notification-infobip")
    ResponseEntity<ResponseObj> smsNotifyUserInfobip(@RequestBody SmsEvent smsEvent, @RequestHeader("Authorization") String token);

    @PostMapping("/sms-notification-twilio")
    ResponseEntity<ResponseObj> smsNotifyUserTwilio(@RequestBody SmsEvent smsEvent, @RequestHeader("Authorization") String token);

    @GetMapping("/admin/view-active-sms-charge")
    ResponseEntity<ResponseObj<SMSChargeResponse>> getActiveSMSCharge(@RequestHeader("Authorization") String token);

    @GetMapping("/admin/active-sms-gateway")
    ResponseEntity<ResponseObj<SMSGatewayResponse>> getActiveSMSGateway(@RequestHeader("Authorization") String token);


}
