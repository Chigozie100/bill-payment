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
//    ApiResponse<?> createBillerCategory(String token, String name, String description, Long categoryId,boolean isActive);
//    ApiResponse<?> createBillerProduct(String token, BillerProductDto billerProductDto);
//    ApiResponse<?> createBillerProductBundle(String token, BigDecimal amount, String name, Long billerProductId, boolean isActive);
//    ApiResponse<?> fetchAllBillerCategory(String token);
//    ApiResponse<?> fetchAllBillerProduct(String token);
//    ApiResponse<?> fetchAllBillerProductBundle(String token,Long billerProductId);
    ApiResponse<?> createTransactionHistory(TransactionDto transactionDto,String email);

//    ApiResponse<?> filterAllBillerCategory(String token,Long categoryId, int pageNo, int pageSize);
//    ApiResponse<?> filterAllBillerProduct(String token,Long billerCategoryId, int pageNo, int pageSize);
//    ApiResponse<?> filterAllBillerProductBundle(String token,Long billerProductId, int pageNo, int pageSize);

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
