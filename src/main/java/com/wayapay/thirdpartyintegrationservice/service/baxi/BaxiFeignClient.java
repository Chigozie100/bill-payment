package com.wayapay.thirdpartyintegrationservice.service.baxi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.ws.rs.QueryParam;

@FeignClient(name = "baxi-feign-client", url = "${app.config.baxi.base-url}")
public interface BaxiFeignClient {

    String X_API_KEY = "x-api-key";

    @GetMapping("/billers/category/all")
    CategoryResponse getCategory(@RequestHeader(X_API_KEY) String xApiKey);

    @PostMapping("/billers/services/category")
    GetAllBillersByCategoryResponse getAllBillersByCategory(@RequestHeader(X_API_KEY) String xApiKey, @QueryParam("service_type") String serviceType);

}
