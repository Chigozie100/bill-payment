package com.wayapay.thirdpartyintegrationservice.v2.service;

import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.*;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

public interface AdminBillPaymentService {
    ApiResponse<?> createBillProviderCharges(HttpServletRequest request, String token, Long serviceProviderId, Long serviceProviderCategoryId, CreateChargeDto createChargeDto);
    ApiResponse<?> updateBillProviderCharges(HttpServletRequest request,String token,Long id, CreateChargeDto createChargeDto);
    ApiResponse<?> activateCategory(HttpServletRequest request,String token,Long id, boolean isActive);
    ApiResponse<?> activateServiceProvider(HttpServletRequest request,String token,Long id, boolean isActive);
    ApiResponse<?> createCategory(HttpServletRequest request,String token, BillCategoryName name, String description);
    ApiResponse<?> fetchAllCategory(HttpServletRequest request,String token);
    ApiResponse<?> createServiceProvider(HttpServletRequest req,String token, CategoryDto request);
    ApiResponse<?> updateServiceProvider(HttpServletRequest req,String token, UpdateServiceProvider request,Long serviceProviderId);
    ApiResponse<?> fetchAllServiceProvider(HttpServletRequest request,String token);

    ApiResponse<?> updateBiller(HttpServletRequest request,String token, Long id,boolean isActive);
    ApiResponse<?> updateBillerProduct(HttpServletRequest request,String token, Long id,BigDecimal amount, boolean isActive);
    ApiResponse<?> updateBillerProductBundle(HttpServletRequest request,String token, Long id, BigDecimal amount, boolean isActive);
    ApiResponse<?> updateBillerCategory(HttpServletRequest request,String token,Long id, boolean isActive);
    ApiResponse<?> updateCategory(HttpServletRequest request,String token, Long id, boolean isActive);

    ApiResponse<?> createTransactionHistory(TransactionDto transactionDto,String email);

    ApiResponse<?> filterAllCategory(HttpServletRequest request,String token, int pageNo, int pageSize);
    ApiResponse<?> filterAllServiceProvider(HttpServletRequest request,String token, int pageNo, int pageSize);

    ApiResponse<?> createServiceProviderCategory(HttpServletRequest request,String token, Long serviceProviderId, String name, String description,String type);
    ApiResponse<?> createServiceProviderBiller(HttpServletRequest request,String token, Long serviceProviderCategoryId, String name, String description,String type);
    ApiResponse<?> createServiceProviderProduct(HttpServletRequest request,String token, Long serviceProviderBillerId, String name, String description,String type,boolean hasBundle,boolean hasTokenValidation);
    ApiResponse<?> createServiceProviderProductBundle(HttpServletRequest request,String token, Long serviceProviderProductId, BigDecimal amount, String name, String description,String type);

    ApiResponse<?> fetchServiceProviderCategory(HttpServletRequest request,String token, Long serviceProviderId, int pageNo, int pageSize);
    ApiResponse<?> fetchServiceProviderBiller(HttpServletRequest request,String token, Long serviceProviderCategoryId, int pageNo, int pageSize);
    ApiResponse<?> fetchServiceProviderProduct(HttpServletRequest request,String token, Long serviceProviderBillerId, int pageNo, int pageSize);
    ApiResponse<?> fetchServiceProviderProductBundle(HttpServletRequest request,String token, Long serviceProviderProductId, int pageNo, int pageSize);

    ApiResponse<?> fetchBillChargesForProviders(HttpServletRequest request,String token, Long serviceProviderId, int pageNo, int pageSize);

    ApiResponse<?> adminAnalysis(HttpServletRequest request,String token);
    ApiResponse<?> fetchOrFilterTransactionHistory(HttpServletRequest request,String token,String endDate, String field, String value, int pageNo, int pageSize);
}
