package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;

import java.util.List;

public interface IThirdPartyService {

    List<CategoryResponse> getCategory() throws ThirdPartyIntegrationException;
    List<BillerResponse> getAllBillersByCategory(String category) throws ThirdPartyIntegrationException;
    PaymentItemsResponse getCustomerValidationFormByBiller(String categoryId, String billerId) throws ThirdPartyIntegrationException;
    CustomerValidationResponse validateCustomerValidationFormByBiller(CustomerValidationRequest request) throws ThirdPartyIntegrationException;
    PaymentResponse processPayment(PaymentRequest request, String transactionId) throws ThirdPartyIntegrationException;

}
