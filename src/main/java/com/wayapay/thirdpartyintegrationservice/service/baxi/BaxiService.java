package com.wayapay.thirdpartyintegrationservice.service.baxi;

import com.wayapay.thirdpartyintegrationservice.config.AppConfig;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaxiService implements IThirdPartyService {

    private AppConfig appConfig;
    private BaxiFeignClient feignClient;
    private static final String SUCCESS_RESPONSE_CODE = "00";
    private static Map<String, String> serviceTypes = new HashMap<>();

    public BaxiService(AppConfig appConfig, BaxiFeignClient feignClient) {
        this.appConfig = appConfig;
        this.feignClient = feignClient;
    }

    @Override
    public List<String> getCategory() throws ThirdPartyIntegrationException {
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
            categoryResponse.getData().forEach(categoryDetail -> serviceTypes.put(categoryDetail.getName(), categoryDetail.getService_type()));
            return new ArrayList<>(serviceTypes.keySet());
        }

        log.error("response from BAxi while trying to get categories is {}", categoryResponse);
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, categoryResponse.getMessage());
    }

    @Override
    public List<String> getAllBillersByCategory(String category) throws ThirdPartyIntegrationException {

        Optional<GetAllBillersByCategoryResponse> allBillersByCategoryOptional = Optional.empty();
        try {
            allBillersByCategoryOptional = Optional.of(feignClient.getAllBillersByCategory(appConfig.getBaxi().getXApiKey(), serviceTypes.get(category)));
        } catch (FeignException e) {
            log.error("Unable to fetch all billers by category => {}", serviceTypes.get(category), e);
        }

        if (allBillersByCategoryOptional.isPresent()){
            log.error("No response from baxi while trying to fetch billers by category");
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch billers by category");
        }

        GetAllBillersByCategoryResponse getAllBillersByCategoryResponse = allBillersByCategoryOptional.get();
        if(getAllBillersByCategoryResponse.getCode().equals(SUCCESS_RESPONSE_CODE)){
            return getAllBillersByCategoryResponse.getData().stream().map(BillerDetail::getServiceType).collect(Collectors.toList());
        }

        log.error("response from Baxi while trying to get billers by category is {}", getAllBillersByCategoryResponse);
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, getAllBillersByCategoryResponse.getMessage());

    }

    @Override
    public List<String> getCustomerValidationFormByBiller(String billerName) throws ThirdPartyIntegrationException {
        return new ArrayList<>();
    }

    @Override
    public void validateCustomerValidationFormByBiller() throws ThirdPartyIntegrationException {

    }

    @Override
    public void processPayment() throws ThirdPartyIntegrationException {

    }
}
