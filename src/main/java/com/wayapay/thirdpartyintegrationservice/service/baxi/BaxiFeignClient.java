package com.wayapay.thirdpartyintegrationservice.service.baxi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import static com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiConstants.X_API_KEY;

@FeignClient(name = "baxi-feign-client", url = "${app.config.baxi.base-url}")
public interface BaxiFeignClient {

    @GetMapping("/billers/category/all")
    CategoryResponse getCategory(@RequestHeader(X_API_KEY) String xApiKey);

    @GetMapping("/services/{service_type}/providers")
    GetAllBillersByCategoryResponse getAllBillersByCategory(@RequestHeader(X_API_KEY) String xApiKey, @PathVariable("service_type") String serviceType);

    @GetMapping("/services/{service_type}/billers")
    GetAllBillersByCategoryResponse getAllBillersByElectricityCategory(@RequestHeader(X_API_KEY) String xApiKey, @PathVariable("service_type") String serviceType);

    @PostMapping("/services/databundle/bundles")
    DataBundleResponse getDataBundles(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("service_type") String serviceType);

    @PostMapping("/services/multichoice/list")
    CableTvPlanResponse getCableTvPlans(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("service_type") String serviceType);

    @PostMapping("/services/epin/bundles")
    EPinBundleResponse getEpinBundles(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("service_type") String serviceType);

    @PostMapping("/services/electricity/verify")
    ElectricityVerificationResponse verifyCustomerElectrictyDetail(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody ElectricityRequest electricityRequest);

}
