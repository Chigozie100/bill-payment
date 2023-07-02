package com.wayapay.thirdpartyintegrationservice.v2.proxyclient;

import com.wayapay.thirdpartyintegrationservice.v2.dto.request.InfoResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.TransferFromWalletPojo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(contextId = "wallet-service" ,name = "wallet-feign-client", url = "${app.config.wallet.base-url}")
public interface WalletProxy {

    @GetMapping(path = "/api/v1/wallet/user-account/{accountNo}")
    ResponseEntity<InfoResponse> getUserWalletByUser(@PathVariable("accountNo") String accountNo, @RequestHeader("Authorization") String token);

    @PostMapping(path="/api/v1/wallet/event/charge/payment")
    ResponseEntity<String> transferFromUserToWaya(@RequestBody TransferFromWalletPojo transfer, @RequestHeader("Authorization") String token, @RequestHeader("pin") String pin);

}
