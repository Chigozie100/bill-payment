package com.wayapay.thirdpartyintegrationservice.service.dispute;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dispute-feign-client", url = "${app.config.dispute.base-url}")
public interface DisputeServiceFeignClient {

    @PostMapping
    DisputeResponse logTransactionAsDispute(@RequestBody DisputeRequest disputeRequest);

}
