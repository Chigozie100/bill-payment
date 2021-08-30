package com.wayapay.thirdpartyintegrationservice.service.logactivity;

import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseObj;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "log-service-feign-client", url = "${app.config.log-service.base-url}")
public interface LogFeignClient {

    @PostMapping("/api/v1/log/create")
    ResponseEntity<ResponseObj<LogRequest>> createLog(@RequestBody LogRequest logRequest, @RequestHeader("Authorization") String token);

    ///api/v1/log/create  ---Create an event log

    ///api/v1/log/update/12 http://46.101.41.187:8083/api/v1/log/create

}
