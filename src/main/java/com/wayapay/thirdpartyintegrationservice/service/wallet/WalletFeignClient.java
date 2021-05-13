package com.wayapay.thirdpartyintegrationservice.service.wallet;

import com.wayapay.thirdpartyintegrationservice.dto.MainWalletResponse;
import com.wayapay.thirdpartyintegrationservice.dto.TransactionRequest;
import com.wayapay.thirdpartyintegrationservice.dto.TransferFromWalletPojo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "wallet-feign-client", url = "${app.config.wallet.base-url}")
public interface WalletFeignClient {

    @PostMapping("/wallet/wallet2wallet")
    ResponseHelper wallet2wallet(@RequestBody FundTransferRequest fundTransferRequest);

    @PostMapping("/wallet/dotransaction")
    FundTransferResponse doTransaction(@RequestBody FundTransferRequest fundTransferRequest);
    
    //NEW WALLET
    @PostMapping(path="/transaction/new/transfer/to/user", produces = "application/json")
	public ResponseEntity<TransactionRequest> transferToUser(@RequestParam TransferFromWalletPojo transfer, @RequestParam("command") String command, @RequestHeader("Authorization") String token);

    @GetMapping("/wallet/find/by/userId/{userId}")
	public List<MainWalletResponse> getWalletById(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);

    @GetMapping("/wallet/get/default/wallet")
    MainWalletResponse getDefaultWallet(@RequestHeader("Authorization") String token);

}
