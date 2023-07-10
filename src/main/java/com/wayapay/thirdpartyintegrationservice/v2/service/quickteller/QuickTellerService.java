package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller;

import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto.SendPaymentAdviceRequest;

public interface QuickTellerService {

    ApiResponse<?> fetchCategories(String token);
    ApiResponse<?> fetchBiller(String token, String categoryId);
    ApiResponse<?> fetchHeaders(String token, String url);
    ApiResponse<?> getPayItem(String token, String billerId);
    ApiResponse<?> createQuickTellerServiceProviderCategory(String token, Long serviceProviderId);
    ApiResponse<?> createQuickTellerServiceProviderBiller(String token, Long serviceProviderId, Long serviceProviderCategoryId);
    ApiResponse<?> createQuickTellerServiceProviderProductByBiller(String token, Long serviceProviderId, Long serviceProviderCategoryId);
    ApiResponse<?> verifyCustomerAccountNumberOrSmartCardOrMeterNumber(String type, String account, String categoryType);
    ApiResponse<?> makeBillsPaymentRequest(SendPaymentAdviceRequest request, String categoryType);
    ApiResponse<?> verifyQuickTellerTransactionRefAdmin(String token, String reference);

}
