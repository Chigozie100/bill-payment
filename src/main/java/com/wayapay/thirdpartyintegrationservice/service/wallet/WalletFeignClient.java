/*
package com.wayapay.thirdpartyintegrationservice.service.wallet;

import com.wayapay.thirdpartyintegrationservice.service.baxi.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import static com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiConstants.X_API_KEY;

@FeignClient(name = "wallet-feign-client", url = "${app.config.baxi.base-url}")
public interface WalletFeignClient {

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

    @PostMapping("/services/namefinder/query")
    NameFinderQueryResponse nameFinderEnquiry(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("service_type") String serviceType, @RequestParam("account_number") String account_number);

    @PostMapping("/services/multichoice/addons")
    CableTvAddonsResponse getCableTvAddons(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("product_code") String productCode, @RequestParam("service_type") String serviceType);

    @PostMapping("/services/electricity/verify")
    ElectricityVerificationResponse verifyCustomerElectricityDetail(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody ElectricityRequest electricityRequest);

    @PostMapping("/services/airtime/request")
    AirtimePaymentResponse airtimePayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("phone") String phone, @RequestParam("amount") String amount, @RequestParam("service_type") String serviceType, @RequestParam("plan") String plan, @RequestParam("agentId") String agentId, @RequestParam("agentReference") String agentReference);

    @PostMapping("/services/databundle/request")
    BundlePaymentResponse bundlePayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("phone") String phone, @RequestParam("amount") String amount, @RequestParam("service_type") String serviceType, @RequestParam("datacode") String datacode, @RequestParam("agentId") String agentId, @RequestParam("agentReference") String agentReference);

    @PostMapping("/services/epin/request")
    EPinPaymentResponse epinPayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("service_type") String serviceType, @RequestParam("numberOfPins") String numberOfPins, @RequestParam("pinValue") String pinValue, @RequestParam("amount") String amount, @RequestParam("agentId") String agentId, @RequestParam("agentReference") String agentReference);

    @PostMapping("/services/multichoice/request")
    CablePaymentResponse cableTvPayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("smartcard_number") String smartcard_number,  @RequestParam("total_amount") String total_amount, @RequestParam("product_code") String product_code, @RequestParam("product_monthsPaidFor") String product_monthsPaidFor, @RequestParam("addon_code") String addon_code, @RequestParam("addon_monthsPaidFor") String addon_monthsPaidFor, @RequestParam("service_type") String serviceType, @RequestParam("agentId") String agentId, @RequestParam("agentReference") String agentReference);

    @PostMapping("/services/electricity/request")
    ElectricPaymentResponse electricityPayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody ElectricPaymentRequest electricPaymentRequest);

}
*/
