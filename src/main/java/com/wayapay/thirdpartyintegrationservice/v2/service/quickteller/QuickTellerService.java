package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller;

import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto.SendPaymentAdviceRequest;

import javax.servlet.http.HttpServletRequest;

public interface QuickTellerService {

    ApiResponse<?> fetchCategories(HttpServletRequest request, String token);
    ApiResponse<?> fetchBiller(HttpServletRequest request,String token, String categoryId);
    ApiResponse<?> fetchHeaders(HttpServletRequest request,String token, String url);
    ApiResponse<?> getPayItem(HttpServletRequest request,String token, String billerId);
    ApiResponse<?> createQuickTellerServiceProviderCategory(HttpServletRequest request,String token, Long serviceProviderId);
    ApiResponse<?> createQuickTellerServiceProviderBiller(HttpServletRequest request,String token, Long serviceProviderId, Long serviceProviderCategoryId);
    ApiResponse<?> createQuickTellerServiceProviderProductByBiller(HttpServletRequest request,String token, Long serviceProviderId, Long serviceProviderCategoryId);
    ApiResponse<?> verifyCustomerAccountNumberOrSmartCardOrMeterNumber(String type, String account, String categoryType);
    ApiResponse<?> makeBillsPaymentRequest(SendPaymentAdviceRequest request, String categoryType);
    ApiResponse<?> verifyQuickTellerTransactionRefAdmin(HttpServletRequest request,String token, String reference);

}
