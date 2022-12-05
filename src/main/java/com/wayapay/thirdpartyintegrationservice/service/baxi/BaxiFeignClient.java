package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.wayapay.thirdpartyintegrationservice.service.BettingValidationResponse;
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
    DataBundleResponse getDataBundles(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody DataBundleRequest serviceType);

    @PostMapping("/services/multichoice/list")
    CableTvPlanResponse getCableTvPlans(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody CableTvRequest serviceType);

    @PostMapping("/services/epin/bundles")
    EPinBundleResponse getEpinBundles(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("service_type") String serviceType);

    @PostMapping("/services/namefinder/query")
    NameFinderQueryResponse nameFinderEnquiry(@RequestHeader(X_API_KEY) String xApiKey, @RequestParam("service_type") String serviceType, @RequestParam("account_number") String account_number);

    @PostMapping("/services/multichoice/addons")
    CableTvAddonsResponse getCableTvAddons(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody AddonRequest request);

    @PostMapping("/services/electricity/verify")
    ElectricityVerificationResponse verifyCustomerElectricityDetail(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody ElectricityRequest electricityRequest);

    @PostMapping("/services/airtime/request")
    AirtimePaymentResponse airtimePayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody AirtimePayment payment);  // @RequestParam("phone") String phone, @RequestParam("amount") String amount, @RequestParam("service_type") String serviceType, @RequestParam("plan") String plan, @RequestParam("agentId") String agentId, @RequestParam("agentReference") String agentReference

    @PostMapping("/services/databundle/request")
    BundlePaymentResponse bundlePayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody DataPayment payment);

    @PostMapping("/services/epin/request")
    EPinPaymentResponse epinPayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody EPinPaymentRequest request);

    @PostMapping("/services/multichoice/request")
    CablePaymentResponse cableTvPayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody CablePaymentRequest request);
//@RequestParam("smartcard_number") String smartcard_number,  @RequestParam("total_amount") String total_amount, @RequestParam("product_code") String product_code, @RequestParam("product_monthsPaidFor") String product_monthsPaidFor, @RequestParam("addon_code") String addon_code, @RequestParam("addon_monthsPaidFor") String addon_monthsPaidFor, @RequestParam("service_type") String serviceType, @RequestParam("agentId") String agentId, @RequestParam("agentReference") String agentReference

    @PostMapping("/services/electricity/request")
    ElectricPaymentResponse electricityPayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody ElectricPaymentRequest electricPaymentRequest);

    @GetMapping("/services/superagent/transaction/requery?agentReference={agentReference}")
    Object reQueryTransaction(@RequestHeader(X_API_KEY) String xApiKey, @PathVariable("agentReference") String agentReference);


    @PostMapping("/services/betting/request")
    BettingPaymentRespose bettingPayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody BettingRequest bettingRequest);

    @GetMapping("/services/namefinder/query")
    BettingValidationResponse namefinder(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody ValidateBettingAccount request);

    @PostMapping("/services/insurance/request")
    BettingPaymentRespose insurancePayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody InsurancePaymentRequest insurancePaymentRequest);

    @PostMapping("/services/vehiclepaper/request")
    BettingPaymentRespose carPaddyPayment(@RequestHeader(X_API_KEY) String xApiKey, @RequestBody InsurancePaymentRequest insurancePaymentRequest);


//https://api.staging.baxibap.com/services/vehiclepaper/request

    //https://api.staging.baxibap.com/services/namefinder/query
    //https://api.staging.baxibap.com/services/superagent/transaction/requery?agentReference=bap-1234513
}
