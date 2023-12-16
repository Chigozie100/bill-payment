package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller;

import com.wayapay.thirdpartyintegrationservice.config.ClientConfiguration;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import static com.wayapay.thirdpartyintegrationservice.v2.dto.Constants.*;


@FeignClient(name = "quickteller-feign-client", url = "${app.config.quickteller.base-url}",configuration = ClientConfiguration.class)
public interface QuickTellerProxy {

    @GetMapping(path = "/api/v2/quickteller/categorys")
    CategoryResponse getCategory(@RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

    @GetMapping(path = "/api/v2/quickteller/billers")
    GetAllBillersResponse getAllBillers(@RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

    @GetMapping(path = "/api/v2/quickteller/billers/{billerId}/paymentitems")
    GetBillerPaymentItemResponse getBillerPaymentItems(@PathVariable("billerId") String billerId, @RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod, @RequestHeader(TERMINAL_ID) String terminalId);

    @PostMapping(path = "/api/v2/quickteller/customers/validations")
    QuickTellerCustomerValidationResponse validateCustomerInfo(@RequestBody QuickTellerCustomerValidationRequest request, @RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod, @RequestHeader(TERMINAL_ID) String terminalId);

    @PostMapping(path = "/api/v2/quickteller/payments/advices")
    SendPaymentAdviceResponse sendPaymentAdvice(@RequestBody SendPaymentAdviceRequest request, @RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod, @RequestHeader(TERMINAL_ID) String terminalId);

    @GetMapping(path = "/api/v2/quickteller/transactions")
    QueryTransactionResponse getQueryTransaction(@RequestParam String requestreference, @RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod, @RequestHeader(TERMINAL_ID) String terminalId);

    @GetMapping(path = "/api/v2/quickteller/categorys/{billerId}/billers")
    GetAllBillersResponse getBillerByCategoryId(@PathVariable String billerId,@RequestHeader(AUTHORIZATION) String authorisation, @RequestHeader(SIGNATURE) String signature, @RequestHeader(NONCE) String nonce, @RequestHeader(TIMESTAMP) String timestamp, @RequestHeader(SIGNATURE_METHOD) String signatureMethod);

}
