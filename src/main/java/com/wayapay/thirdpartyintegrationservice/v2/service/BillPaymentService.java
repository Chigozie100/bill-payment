package com.wayapay.thirdpartyintegrationservice.v2.service;

import com.wayapay.thirdpartyintegrationservice.v2.dto.request.*;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;

public interface BillPaymentService {

    ApiResponse<?> fetchAllBillCategory(String token);
    ApiResponse<?> verifyCustomerAccountOrToken(String token, ValidateCustomerToken customerToken);
    ApiResponse<?> fetchBillersByCategory(String token, Long categoryId);
    ApiResponse<?> fetchAllProductByBiller(String token, Long serviceProviderBillerId);
    ApiResponse<?> fetchAllBundleByProduct(String token, Long serviceProviderProductId);
    ApiResponse<?> makeElectricityPayment(String token,Long serviceProviderBillerId, Long serviceProviderId, ElectricityPaymentDto electricityPaymentDto,String userAccountNumber,String pin);
    ApiResponse<?> makeDataBundlePayment(String token,Long serviceProviderBundleId, Long serviceProviderId, DataBundlePaymentDto dataBundlePaymentDto,String userAccountNumber,String pin,Long serviceProviderBillerId);
    ApiResponse<?> makeAirtimePayment(String token,Long serviceProviderBillerId, Long serviceProviderId, AirtimePaymentDto airtimePaymentDto,String userAccountNumber,String pin);
    ApiResponse<?> makeEpinPayment(String token,Long serviceProviderBundleId, Long serviceProviderId, EpinPaymentDto epinPaymentDto,String userAccountNumber,String pin,Long serviceProviderBillerId);
    ApiResponse<?> makeCableTvPayment(String token,Long serviceProviderBundleId, Long serviceProviderId, CableTvPaymentDto cableTvPaymentDto,String userAccountNumber,String pin,Long serviceProviderBillerId);
    ApiResponse<?> makeBettingPayment(String token,Long serviceProviderBillerId, Long serviceProviderId, BettingPaymentDto bettingPaymentDto,String userAccountNumber,String pin);
    ApiResponse<?> fetchBillTransactionByReference(String token, String reference);
    ApiResponse<?> makeOtherPayment(String token,Long serviceProviderBillerId, Long serviceProviderId, OthersPaymentDto othersPaymentDto,String userAccountNumber, String pin);
//    void reverseFailedBillPaymentTransaction(String reference,String accountNumber);
//    ApiResponse<?> fetchAllAddOnsByProduct(String token, Long serviceProviderProductId, String productCode);
}
