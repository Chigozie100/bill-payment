package com.wayapay.thirdpartyintegrationservice.service.commission;

import com.wayapay.thirdpartyintegrationservice.dto.ApiResponseBody;
import com.wayapay.thirdpartyintegrationservice.util.TransactionType;
import com.wayapay.thirdpartyintegrationservice.util.UserType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "commission-feign-client", url = "${app.config.commission.base-url}")
public interface CommissionFeignClient {

    @GetMapping(path = "/api/v1/user")
    ResponseEntity<String> userCommission(@RequestHeader("Authorization") String token);

    @GetMapping(path = "/api/v1/user/get-user-commission-external/userType/{user_type}/transactionType/{transaction_type}")
    ResponseEntity<ApiResponseBody<UserCommissionDto>> userCommissionExtra(@PathVariable("user_type") UserType user_type, @PathVariable("transaction_type") TransactionType transaction_type, @RequestHeader("Authorization") String token);
    //localhost:8281/commission/api/v1/user/get-user-commission-external/userType/CORPORATE_USER/transactionType/BILLS_PAYMENT

    @PostMapping(path = "/user/add-commission-history")
    ResponseEntity<ApiResponseBody<CommissionDto>> addCommissionHistory(@RequestBody CommissionDto commissionDto, @RequestHeader("Authorization") String token);


    @PostMapping("/track/merchant-commission")
    ResponseEntity<ApiResponseBody<MerchantCommissionTrackerDto>> recordMerchantCommission(@RequestBody MerchantCommissionTrackerDto request, @RequestHeader("Authorization") String token);


}