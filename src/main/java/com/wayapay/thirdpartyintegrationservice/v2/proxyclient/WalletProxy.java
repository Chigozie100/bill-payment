package com.wayapay.thirdpartyintegrationservice.v2.proxyclient;

import com.wayapay.thirdpartyintegrationservice.config.ClientConfiguration;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.InfoResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.ReversalDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.TransferFromWalletPojo;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ReversalRespDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.WalletTxnResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(contextId = "wallet-service" ,name = "wallet-feign-client", url = "${app.config.wallet.base-url}",configuration = ClientConfiguration.class)
public interface WalletProxy {

    @GetMapping(path = "/api/v1/wallet/user-account/{accountNo}")
    ResponseEntity<InfoResponse> getUserWalletByUser(@PathVariable("accountNo") String accountNo, @RequestHeader("Authorization") String token, @RequestHeader(Constants.CLIENT_ID) String clientId, @RequestHeader(Constants.CLIENT_TYPE) String clientType);

    @PostMapping(path="/api/v1/wallet/event/charge/payment")
    ResponseEntity<String> transferFromUserToWaya(@RequestBody TransferFromWalletPojo transfer, @RequestHeader("Authorization") String token, @RequestHeader("pin") String pin, @RequestHeader(Constants.CLIENT_ID) String clientId, @RequestHeader(Constants.CLIENT_TYPE) String clientType);

    @GetMapping(path = "/api/v1/wallet/account/transactions/{tranId}")
    ApiResponse<?> fetchTransactionById(@RequestHeader("Authorization") String token, @PathVariable String tranId, @RequestHeader(Constants.CLIENT_ID) String clientId, @RequestHeader(Constants.CLIENT_TYPE) String clientType);

    @GetMapping(path = "/api/v1/wallet/fetchByReferenceNumber/{referenceNumber}")
    WalletTxnResponse fetchTransactionByPaymentReference(@RequestHeader("Authorization") String token, @PathVariable String referenceNumber, @RequestHeader(Constants.CLIENT_ID) String clientId, @RequestHeader(Constants.CLIENT_TYPE) String clientType);

    @PostMapping(path = "/api/v1/wallet/transaction/reverse")
    ReversalRespDto doPaymentReversal(@RequestHeader("Authorization") String token, @RequestBody ReversalDto reversalDto, @RequestHeader(Constants.CLIENT_ID) String clientId, @RequestHeader(Constants.CLIENT_TYPE) String clientType);
}
