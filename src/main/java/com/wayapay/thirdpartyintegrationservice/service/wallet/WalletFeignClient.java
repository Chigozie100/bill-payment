package com.wayapay.thirdpartyintegrationservice.service.wallet;

import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-feign-client", url = "${app.config.wallet.base-url}")
public interface WalletFeignClient {

    @PostMapping("/wallet/wallet2wallet")
    ResponseHelper wallet2wallet(@RequestBody FundTransferRequest fundTransferRequest);

    @PostMapping("/wallet/dotransaction")
    FundTransferResponse doTransaction(@RequestBody FundTransferRequest fundTransferRequest);

}
