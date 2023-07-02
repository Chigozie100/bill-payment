package com.wayapay.thirdpartyintegrationservice.v2.service.baxi;

import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto.*;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto.BundlePaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.wayapay.thirdpartyintegrationservice.v2.dto.Constants.BAXI_X_API_KEY;

@FeignClient(contextId = "baxi-service" ,name = "baxi-feign-client", url = "${app.config.baxi.base-url}")
public interface BaxiProxy {

    @GetMapping(path = "/services/billers/category/all")
    CategoryResponse fetchAllCategory(@RequestHeader(BAXI_X_API_KEY) String xApiKey);

    @PostMapping(path = "/services/billers/services/category")
    ServiceBillerLogoResponse fetchServiceProviderLogoByCategoryName(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody ServiceCategoryName categoryName);

    @GetMapping(path = "/services/billers/services/list")
    ServiceBillerLogoResponse fetchAllCategoryProviderLogo(@RequestHeader(BAXI_X_API_KEY) String xApiKey);

    @GetMapping(path = "/services/{service_type}/providers")
    GetAllBillersByCategoryResponse getAllBillersByCategory(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @PathVariable("service_type") String serviceType);

    @GetMapping(path = "/services/{service_type}/billers")
    GetAllBillersByCategoryResponse getAllBillersByElectricityAndBettingCategory(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @PathVariable("service_type") String serviceType);

    @GetMapping(path = "/services/{service_type}/billers")
    GetAllElectricityBillersByCategoryResponse getAllBillersByElectricityAndCategory(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @PathVariable("service_type") String serviceType);

    @PostMapping(path = "/services/airtime/request")
    AirtimePaymentResponse airtimePayment(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody AirtimePayment payment);

    @PostMapping(path = "/services/databundle/request")
    BundlePaymentResponse bundlePayment(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody DataPayment payment);

    @PostMapping(path = "/services/databundle/bundles")
    DataBundleResponse getDataBundles(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody DataBundleRequest serviceType);

    @PostMapping(path = "/services/electricity/verify")
    ElectricityVerificationResponse verifyCustomerElectricityDetail(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @Valid@RequestBody ElectricityRequest electricityRequest);

    @PostMapping(path = "/services/electricity/request")
    ElectricPaymentResponse electricityPayment(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody ElectricPaymentRequest electricPaymentRequest);

    @PostMapping(path = "/services/multichoice/request")
    CablePaymentResponse cableTvPayment(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody CablePaymentRequest request);

    @PostMapping(path = "/services/multichoice/list")
    CableTvPlanResponse fetchCableTvProductAndBundle(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody CableTvRequest serviceType);

    @PostMapping(path = "/services/multichoice/addons")
    CableTvAddonsResponse getCableTvAddons(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody AddonRequest request);

    @PostMapping(path = "/services/epin/bundles")
    EPinBundleResponse getEpinBundles(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @Valid @RequestBody EpinBundleDto epinBundleDto);

    @PostMapping(path = "/services/epin/request")
    EPinPaymentResponse epinPayment(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody EPinPaymentRequest request);

    @PostMapping(path = "/services/namefinder/query")
    NameFinderQueryResponse nameFinderEnquiry(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @Valid @RequestBody NameQueryDto nameQueryDto);

    @PostMapping(path = "/services/betting/request")
    BettingPaymentRespose bettingPayment(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody BettingRequest bettingRequest);

    @PostMapping(path = "/services/insurance/request")
    BettingPaymentRespose insurancePayment(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody InsurancePaymentRequest insurancePaymentRequest);

    @PostMapping(path = "/services/vehiclepaper/request")
    BettingPaymentRespose carPaddyPayment(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @RequestBody InsurancePaymentRequest insurancePaymentRequest);

    @GetMapping("/services/superagent/transaction/requery?agentReference={agentReference}")
    Object reQueryTransaction(@RequestHeader(BAXI_X_API_KEY) String xApiKey, @PathVariable("agentReference") String agentReference);


}
