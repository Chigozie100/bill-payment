package com.wayapay.thirdpartyintegrationservice.service.wallet;

import com.wayapay.thirdpartyintegrationservice.dto.TransferFromWalletPojo;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "wallet-feign-client", url = "${app.config.wallet.base-url}")
public interface WalletFeignClient {

    @GetMapping("/api/v1/wallet/admin/user-account/{accountNo}") //  ===> returns single for adm
    ResponseEntity<InfoResponse> getUserWallet(@PathVariable("accountNo") String accountNo, @RequestHeader("Authorization") String token);
  
    @GetMapping("/api/v1/wallet/user-account/{accountNo}") //  ===> returns single
    ResponseEntity<InfoResponse> getUserWalletByUser(@PathVariable("accountNo") String accountNo, @RequestHeader("Authorization") String token);


    @PostMapping(path="/api/v1/wallet/event/charge/payment")
    ResponseEntity<String> transferFromUserToWaya(@RequestBody TransferFromWalletPojo transfer, @RequestHeader("Authorization") String token);

    @GetMapping("/api/v1/wallet/commission-accounts/{userId}")
    ResponseEntity<ApiResponseBody<NewWalletResponse>> getUserCommissionWallet(@PathVariable("userId") String userId, @RequestHeader("Authorization") String token);

    // Tested and working well 30/10/2021 to be done today
    @GetMapping("/api/v1/wallet/waya/official/account")
    ResponseEntity<InfoResponseList> getWayaOfficialWallet(@RequestHeader("Authorization") String token);

    @PostMapping("/api/v1/wallet/official/user/transfer")
    ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> refundFailedTransaction(@RequestBody TransferFromOfficialToMainWallet transfer, @RequestHeader("Authorization") String token);

    @PostMapping("/api/v1/wallet/admin/commission/payment")
    ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> officialCommissionToUserCommission(@RequestBody TransferFromWalletPojo transfer, @RequestHeader("Authorization") String token);
    
    @GetMapping("/api/v1/wallet/offical-account/{eventID}")
    ResponseEntity<ApiResponseBody<?>> officialAccount(@PathVariable("eventID") String eventID, @RequestHeader("Authorization") String token);
 
    
    @PostMapping("/api/v1/wallet/official/user/transfer")
    ResponseEntity<ApiResponseBody<List<WalletTransactionPojo>>> officialToUserCommission(@RequestBody OfficialToUserCommission transfer, @RequestHeader("Authorization") String token);
    
}
