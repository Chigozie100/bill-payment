package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.dto.BillerResponse;
import com.wayapay.thirdpartyintegrationservice.dto.Item;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentItemsResponse;
import com.wayapay.thirdpartyintegrationservice.dto.SubItem;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wayapay.thirdpartyintegrationservice.service.baxi.BillerCategoryName.*;

@Slf4j
@Service
public class BaxiService implements IThirdPartyService {

    private AppConfig appConfig;
    private BaxiFeignClient feignClient;
    private static final String SUCCESS_RESPONSE_CODE = "00";
    private static final String AMOUNT = "amount";
    private static final String PHONE = "phone";

    public BaxiService(AppConfig appConfig, BaxiFeignClient feignClient) {
        this.appConfig = appConfig;
        this.feignClient = feignClient;
    }

    //todo cache the request and response
    @Override
    public List<com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse> getCategory() throws ThirdPartyIntegrationException {

        Optional<CategoryResponse> categoryResponseOptional = Optional.empty();
        try {
            categoryResponseOptional = Optional.of(feignClient.getCategory(appConfig.getBaxi().getXApiKey()));
        } catch (FeignException e) {
            log.error("Unable to get categories from Baxi ", e);
        }

        if (!categoryResponseOptional.isPresent()){
            log.error("No response from Baxi");
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch categories");
        }

        CategoryResponse categoryResponse = categoryResponseOptional.get();
        if (categoryResponse.getCode().equals(SUCCESS_RESPONSE_CODE)) {
            return categoryResponse.getData().parallelStream().map(category -> new com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse(category.getService_type(), category.getName())).collect(Collectors.toList());
        }

        log.error("response from BAxi while trying to get categories is {}", categoryResponse);
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, categoryResponse.getMessage());
    }

    //todo cache the request and response
    @Override
    public List<BillerResponse> getAllBillersByCategory(String categoryId) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(categoryId)){
            log.error("categoryId provided id null or empty");
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid categoryId");
        }

        Optional<GetAllBillersByCategoryResponse> allBillersByCategoryOptional = Optional.empty();
        try {
            if (categoryId.equalsIgnoreCase(ELECTRICITY)){
                allBillersByCategoryOptional = Optional.of(feignClient.getAllBillersByElectricityCategory(appConfig.getBaxi().getXApiKey(), categoryId));
            } else {
                allBillersByCategoryOptional = Optional.of(feignClient.getAllBillersByCategory(appConfig.getBaxi().getXApiKey(), categoryId));
            }
        } catch (FeignException e) {
            log.error("Unable to fetch all billers by category => {}", categoryId, e);
        }

        GetAllBillersByCategoryResponse getAllBillersByCategoryResponse = allBillersByCategoryOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch billers by category"));
        if(getAllBillersByCategoryResponse.getCode().equals(SUCCESS_RESPONSE_CODE)){
            return getAllBillersByCategoryResponse.getData().getProviders().parallelStream().map(billerDetail -> new BillerResponse(billerDetail.getService_type(), billerDetail.getName(), categoryId)).collect(Collectors.toList());
        }

        log.error("response from Baxi while trying to get billers by category is {}", getAllBillersByCategoryResponse);
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getAllBillersByCategoryResponse.getMessage());
    }

    @Override
    public PaymentItemsResponse getCustomerValidationFormByBiller(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        if (CommonUtils.isEmpty(categoryId) || CommonUtils.isEmpty(billerId)){
            log.error("categoryId => {} or billerId => {} is empty/null ", categoryId, billerId);
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category or biller provided");
        }

        switch (categoryId){
            case AIRTIME:
                return getAirtimePaymentItems(categoryId, billerId);
            case DATABUNDLE:
                return getDataBundlePaymentItems(categoryId, billerId);
            case CABLETV:
                return getCableTvPaymentItems(categoryId, billerId);
            case ELECTRICITY:
                return getElectricityPaymentItems(categoryId, billerId);
            case EPIN:
                return getEPinBundlePaymentItems(categoryId, billerId);
            default:
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid CategoryId provided");
        }
    }

    @Override
    public void validateCustomerValidationFormByBiller() throws ThirdPartyIntegrationException {

    }

    @Override
    public void processPayment() throws ThirdPartyIntegrationException {

    }

    private PaymentItemsResponse getAirtimePaymentItems(String categoryId, String billerId){
        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item(PHONE));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemPlan = new Item();
        itemPlan.setParamName("plan");
        itemPlan.getSubItems().add(new SubItem("prepaid"));
        itemPlan.getSubItems().add(new SubItem("postpaid"));
        paymentItemsResponse.getItems().add(itemPlan);
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getDataBundlePaymentItems(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        Optional<DataBundleResponse> bundleResponseOptional = Optional.empty();
        try {
            bundleResponseOptional = Optional.of(feignClient.getDataBundles(appConfig.getBaxi().getXApiKey(), billerId));
        } catch (FeignException e) {
            log.error("Unable to fetch Data Bundles", e);
        }
        DataBundleResponse dataBundleResponse = bundleResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch data bundles"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item(PHONE));
        paymentItemsResponse.getItems().add(new Item(AMOUNT));
        Item itemBundles = new Item();
        itemBundles.setParamName("bundles");
        itemBundles.setIsAccountFixed(Boolean.TRUE);
        dataBundleResponse.getData().forEach(dataBundle -> itemBundles.getSubItems().add(new SubItem(dataBundle.getDatacode(), dataBundle.getName(), dataBundle.getPrice(), dataBundle.getPrice())));
        paymentItemsResponse.getItems().add(itemBundles);
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getCableTvPaymentItems(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        Optional<CableTvPlanResponse> cableTvPlanResponseOptional = Optional.empty();
        try {
            cableTvPlanResponseOptional = Optional.of(feignClient.getCableTvPlans(appConfig.getBaxi().getXApiKey(), billerId));
        } catch (FeignException e) {
            log.error("Unable to fetch Cable Tv Plans",e );
        }
        CableTvPlanResponse cableTvPlanResponse = cableTvPlanResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch cable tv plans"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item("smartcard_number"));
        paymentItemsResponse.getItems().add(new Item("total_amount"));
        Item itemPlan = new Item();
        itemPlan.setParamName("plan");
        itemPlan.setIsAccountFixed(Boolean.TRUE);
        cableTvPlanResponse.getData().forEach(plan -> {
            Optional<Pricing> pricingOptional = plan.getAvailablePricingOptions().stream().filter(pricing -> pricing.getMonthsPaidFor().equals("1")).findFirst();
            String price = pricingOptional.orElse(new Pricing("0")).getPrice();
            itemPlan.getSubItems().add(new SubItem(plan.getCode(), plan.getName(), price, price));
        });
        paymentItemsResponse.getItems().add(itemPlan);
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getElectricityPaymentItems(String categoryId, String billerId){
        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(true);
        paymentItemsResponse.getItems().add(new Item("account_number"));
        return paymentItemsResponse;
    }

    private PaymentItemsResponse getEPinBundlePaymentItems(String categoryId, String billerId) throws ThirdPartyIntegrationException {

        Optional<EPinBundleResponse> ePinBundleResponseOptional = Optional.empty();
        try {
            ePinBundleResponseOptional = Optional.of(feignClient.getEpinBundles(appConfig.getBaxi().getXApiKey(), billerId));
        } catch (FeignException e) {
            log.error("Unable to fetch Epin Bundles", e);
        }
        EPinBundleResponse ePinBundleResponse = ePinBundleResponseOptional.orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch EPin bundles"));

        //get the params, then also get it sublist
        PaymentItemsResponse paymentItemsResponse = new PaymentItemsResponse(categoryId, billerId);
        paymentItemsResponse.setIsValidationRequired(false);
        paymentItemsResponse.getItems().add(new Item("numberOfPins"));
        Item itemPinValues = new Item();
        itemPinValues.setIsAccountFixed(Boolean.TRUE);
        itemPinValues.setParamName("pinValue");
        ePinBundleResponse.getData().forEach(ePinBundle -> itemPinValues.getSubItems().add(new SubItem(ePinBundle.getAmount(), ePinBundle.getDescription(), ePinBundle.getAmount(), ePinBundle.getAmount())));
        paymentItemsResponse.getItems().add(itemPinValues);
        return paymentItemsResponse;
    }
}

class BillerCategoryName{
    static final String AIRTIME = "airtime";
    static final String DATABUNDLE = "databundle";
    static final String CABLETV = "cabletv";
    static final String ELECTRICITY = "electricity";
    static final String EPIN = "epin";

    private BillerCategoryName(){

    }
}

class BaxiConstants{
    static final String X_API_KEY = "x-api-key";
}
