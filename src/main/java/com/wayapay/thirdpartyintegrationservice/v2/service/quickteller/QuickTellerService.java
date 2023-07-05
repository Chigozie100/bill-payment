package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller;

import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;

public interface QuickTellerService {

    ApiResponse<?> fetchCategories(String token);
    ApiResponse<?> fetchBiller(String token, String categoryId);
    ApiResponse<?> fetchHeaders(String token, String url);
    ApiResponse<?> createQuickTellerServiceProviderCategory(String token, Long serviceProviderId);
    ApiResponse<?> createQuickTellerServiceProviderBiller(String token, Long serviceProviderId, Long serviceProviderCategoryId);
    ApiResponse<?> verifyCustomerAccountNumberOrSmartCardOrMeterNumber(String type, String account, String categoryType);
}
