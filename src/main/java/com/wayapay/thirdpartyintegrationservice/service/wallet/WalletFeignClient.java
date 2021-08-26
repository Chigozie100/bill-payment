package com.wayapay.thirdpartyintegrationservice.service.wallet;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@FeignClient(name = "wallet-feign-client", url = "${app.config.wallet.base-url}")
public interface WalletFeignClient {

    //NEW WALLET
    @PostMapping(path="/transaction/new/transfer/to/user?command=DEBIT")
    TransactionRequest transferToUser(@RequestBody TransferFromWalletPojo transfer, @RequestHeader("Authorization") String token);

    @GetMapping("/wallet/find/by/userId/{userId}")
    List<MainWalletResponse> getWalletById(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);

    @GetMapping("/wallet/get/default/wallet")
    MainWalletResponse getDefaultWallet(@RequestHeader("Authorization") String token);


    @GetMapping("/api/v1/wallet/default/{userId}") //  ===> returns single
    ResponseEntity<String> getDefaultWallet(@PathVariable("userId") String userId, @RequestHeader("Authorization") String token);

    ///api/v1/wallet/event/charge/payment /api/v1/wallet/event/charge/payment
    @PostMapping(path="/api/v1/wallet/event/charge/payment")
    ResponseEntity<String> transferFromUserToWaya(@RequestBody TransferFromWalletPojo transfer, @RequestHeader("Authorization") String token);

    @GetMapping("/api/v1/wallet/commission-accounts/{userId}")
    ResponseEntity<String> getUserCommissionWallet(@PathVariable("userId") String userId, @RequestHeader("Authorization") String token);

    // Get waya commission wallet
    @GetMapping("/api/v1/wallet/commission-wallets/all")
    ResponseEntity<String> getWayaCommissionWallet(@RequestHeader("Authorization") String token);

    // Tested and working well 30/10/2021 to be done today
    @GetMapping("/api/v1/wallet/waya/account")
    ResponseEntity<String> getWayaOfficialWallet(@RequestHeader("Authorization") String token);


    ///api/v1/wallet/create-user
    @PostMapping("/api/v1/wallet/create-user")
    ResponseEntity<String> wayaAdminAddCommissionWalletToCoUser(@RequestHeader("Authorization") String token);


    ///api/v1/wallet/fund/transfer/wallet
    //Transfer from one User Wallet to another wallet

    @PostMapping(path="/api/v1/wallet/fund/transfer/wallet")
    TransactionRequest adminReversFundToUser(@RequestBody TransferFromWalletToWallet transfer, @RequestHeader("Authorization") String token);


}
