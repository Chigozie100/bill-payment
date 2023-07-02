package com.wayapay.thirdpartyintegrationservice.v2.service.baxi;

import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderBiller;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderProduct;

import java.math.BigDecimal;

public interface BaxiService {

    ApiResponse<?> createBaxiServiceProviderCategory(String token, Long serviceProviderId);
    ApiResponse<?> createBaxiServiceProviderBiller(String token, Long serviceProviderCategoryId);
    ApiResponse<?> createBaxiServiceProviderProduct(String token, Long serviceProviderId,Long serviceProviderCategoryId);
    ApiResponse<?> createBaxiServiceProviderBillerLogo(String token, Long serviceProviderId, Long serviceProviderCategoryId);
    ApiResponse<?> verifyCustomerAccountNumberOrSmartCardOrMeterNumber(String type, String account, String categoryType);
    ApiResponse<?> requestElectricityPayment(BigDecimal amount, String type,String phone,String account,String reference);
    ApiResponse<?> requestDataBundlePayment(String productCode,String type,String amount,String phone,String reference);
    ApiResponse<?> requestAirtimePayment(int amount, String plan,String phone,String reference,String type);
    ApiResponse<?> requestEpinPayment(int numberOfPins, int amount, int fixAmount, String type,String reference);
    ApiResponse<?> requestCableTvPayment(String type,String phone,String amount,String reference,String productCode,String smartCardNumber,String monthPaidFor);
    ApiResponse<?> requestBettingPayment(BigDecimal amount, String type,String reference,String accountNumber);


}
