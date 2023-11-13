package com.wayapay.thirdpartyintegrationservice.v2.service;

import com.wayapay.thirdpartyintegrationservice.v2.dto.request.*;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;

import javax.servlet.http.HttpServletRequest;

public interface BillPaymentService {

    ApiResponse<?> fetchAllBillCategory(HttpServletRequest request,String token);
    ApiResponse<?> verifyCustomerAccountOrToken(HttpServletRequest request,String token, ValidateCustomerToken customerToken);
    ApiResponse<?> fetchBillersByCategory(HttpServletRequest request,String token, Long categoryId);
    ApiResponse<?> fetchAllProductByBiller(HttpServletRequest request,String token, Long serviceProviderBillerId);
    ApiResponse<?> fetchAllBundleByProduct(HttpServletRequest request,String token, Long serviceProviderProductId);
    ApiResponse<?> makeElectricityPayment(HttpServletRequest request,String token,Long serviceProviderBillerId, Long serviceProviderId, ElectricityPaymentDto electricityPaymentDto,String userAccountNumber,String pin);
    ApiResponse<?> makeDataBundlePayment(HttpServletRequest request,String token,Long serviceProviderBundleId, Long serviceProviderId, DataBundlePaymentDto dataBundlePaymentDto,String userAccountNumber,String pin,Long serviceProviderBillerId);
    ApiResponse<?> makeAirtimePayment(HttpServletRequest request,String token,Long serviceProviderBillerId, Long serviceProviderId, AirtimePaymentDto airtimePaymentDto,String userAccountNumber,String pin);
    ApiResponse<?> makeEpinPayment(HttpServletRequest request,String token,Long serviceProviderBundleId, Long serviceProviderId, EpinPaymentDto epinPaymentDto,String userAccountNumber,String pin,Long serviceProviderBillerId);
    ApiResponse<?> makeCableTvPayment(HttpServletRequest request,String token,Long serviceProviderBundleId, Long serviceProviderId, CableTvPaymentDto cableTvPaymentDto,String userAccountNumber,String pin,Long serviceProviderBillerId);
    ApiResponse<?> makeBettingPayment(HttpServletRequest request,String token,Long serviceProviderBillerId, Long serviceProviderId, BettingPaymentDto bettingPaymentDto,String userAccountNumber,String pin);
    ApiResponse<?> fetchBillTransactionByReference(HttpServletRequest request,String token, String reference);
    ApiResponse<?> makeOtherPayment(HttpServletRequest request,String token,Long serviceProviderBillerId, Long serviceProviderId, OthersPaymentDto othersPaymentDto,String userAccountNumber, String pin);
//    void reverseFailedBillPaymentTransaction(String reference,String accountNumber);
//    ApiResponse<?> fetchAllAddOnsByProduct(String token, Long serviceProviderProductId, String productCode);
}
