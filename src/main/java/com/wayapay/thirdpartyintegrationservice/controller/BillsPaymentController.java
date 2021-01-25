package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.service.BillsPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/billspay")
public class BillsPaymentController {

    private BillsPaymentService billsPaymentService;

    public BillsPaymentController(BillsPaymentService billsPaymentService) {
        this.billsPaymentService = billsPaymentService;
    }

    @GetMapping("/category")
    public ResponseEntity<ResponseHelper> getAllCategories(){
        try {
            List<CategoryResponse> categoryResponseList = billsPaymentService.getBillsPaymentService().getCategory();
            return ResponseEntity.ok(new SuccessResponse(categoryResponseList));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ResponseHelper> getBillersByCategory(@PathVariable("categoryId") String categoryId){
        try {
            List<BillerResponse> allBillersByCategory = billsPaymentService.getBillsPaymentService().getAllBillersByCategory(categoryId);
            return ResponseEntity.ok(new SuccessResponse(allBillersByCategory));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}/biller/{billerId}")
    public ResponseEntity<ResponseHelper> getBillerPaymentItem(@PathVariable("categoryId") String categoryId, @PathVariable("billerId") String billerId){
        try {
            PaymentItemsResponse customerValidationFormByBiller = billsPaymentService.getBillsPaymentService().getCustomerValidationFormByBiller(categoryId, billerId);
            return ResponseEntity.ok(new SuccessResponse(customerValidationFormByBiller));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/biller/validate")
    public ResponseEntity<ResponseHelper> validateCustomerPaymentItem(@Valid @RequestBody CustomerValidationRequest customerValidationRequest){
        try {
            CustomerValidationResponse customerValidationResponse = billsPaymentService.getBillsPaymentService().validateCustomerValidationFormByBiller(customerValidationRequest);
            return ResponseEntity.ok(new SuccessResponse(customerValidationResponse));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/biller/pay")
    public ResponseEntity<ResponseHelper> customerPayment(@Valid @RequestBody PaymentRequest paymentRequest){
        try {
            PaymentResponse paymentResponse = billsPaymentService.getBillsPaymentService().processPayment(paymentRequest, "testTransactionId");
            return ResponseEntity.ok(new SuccessResponse(paymentResponse));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

}
