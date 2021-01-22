package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.BillerResponse;
import com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentItemsResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;

import java.util.List;

public interface IThirdPartyService {

    List<CategoryResponse> getCategory() throws ThirdPartyIntegrationException;
    List<BillerResponse> getAllBillersByCategory(String category) throws ThirdPartyIntegrationException;
    PaymentItemsResponse getCustomerValidationFormByBiller(String categoryId, String billerId) throws ThirdPartyIntegrationException;
    void validateCustomerValidationFormByBiller() throws ThirdPartyIntegrationException;
    void processPayment() throws ThirdPartyIntegrationException;

}
