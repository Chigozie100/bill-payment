package com.wayapay.thirdpartyintegrationservice.service.wallet;

import com.wayapay.thirdpartyintegrationservice.dto.MainWalletResponse;
import com.wayapay.thirdpartyintegrationservice.dto.TransactionRequest;
import com.wayapay.thirdpartyintegrationservice.dto.TransferFromWalletPojo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

}
