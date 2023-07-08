package com.wayapay.thirdpartyintegrationservice.v2.service;

import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.*;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;

import java.math.BigDecimal;

public interface AdminBillPaymentService {
    ApiResponse<?> createBillProviderCharges(String token,Long serviceProviderId, Long serviceProviderCategoryId, CreateChargeDto createChargeDto);
    ApiResponse<?> updateBillProviderCharges(String token,Long id, CreateChargeDto createChargeDto);
    ApiResponse<?> activateCategory(String token,Long id, boolean isActive);
    ApiResponse<?> activateServiceProvider(String token,Long id, boolean isActive);
    ApiResponse<?> createCategory(String token, BillCategoryName name, String description);
    ApiResponse<?> fetchAllCategory(String token);
    ApiResponse<?> createServiceProvider(String token, CategoryDto request);
    ApiResponse<?> updateServiceProvider(String token, UpdateServiceProvider request,Long serviceProviderId);
    ApiResponse<?> fetchAllServiceProvider(String token);

    ApiResponse<?> updateBiller(String token, Long id,boolean isActive);
    ApiResponse<?> updateBillerProduct(String token, Long id,BigDecimal amount, boolean isActive);
    ApiResponse<?> updateBillerProductBundle(String token, Long id, BigDecimal amount, boolean isActive);
    ApiResponse<?> updateBillerCategory(String token,Long id, boolean isActive);
    ApiResponse<?> updateCategory(String token, Long id, boolean isActive);

    ApiResponse<?> createTransactionHistory(TransactionDto transactionDto,String email);

    ApiResponse<?> filterAllCategory(String token, int pageNo, int pageSize);
    ApiResponse<?> filterAllServiceProvider(String token, int pageNo, int pageSize);

    ApiResponse<?> createServiceProviderCategory(String token, Long serviceProviderId, String name, String description,String type);
    ApiResponse<?> createServiceProviderBiller(String token, Long serviceProviderCategoryId, String name, String description,String type);
    ApiResponse<?> createServiceProviderProduct(String token, Long serviceProviderBillerId, String name, String description,String type,boolean hasBundle,boolean hasTokenValidation);
    ApiResponse<?> createServiceProviderProductBundle(String token, Long serviceProviderProductId, BigDecimal amount, String name, String description,String type);

    ApiResponse<?> fetchServiceProviderCategory(String token, Long serviceProviderId, int pageNo, int pageSize);
    ApiResponse<?> fetchServiceProviderBiller(String token, Long serviceProviderCategoryId, int pageNo, int pageSize);
    ApiResponse<?> fetchServiceProviderProduct(String token, Long serviceProviderBillerId, int pageNo, int pageSize);
    ApiResponse<?> fetchServiceProviderProductBundle(String token, Long serviceProviderProductId, int pageNo, int pageSize);

}
