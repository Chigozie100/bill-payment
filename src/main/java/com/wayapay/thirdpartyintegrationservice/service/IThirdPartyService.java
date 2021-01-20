package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;

import java.util.List;

public interface IThirdPartyService {

    List<String> getCategory() throws ThirdPartyIntegrationException;
    List<String> getAllBillersByCategory(String category) throws ThirdPartyIntegrationException;
    List<String> getCustomerValidationFormByBiller(String billerName) throws ThirdPartyIntegrationException;
    void validateCustomerValidationFormByBiller() throws ThirdPartyIntegrationException;
    void processPayment() throws ThirdPartyIntegrationException;

}
