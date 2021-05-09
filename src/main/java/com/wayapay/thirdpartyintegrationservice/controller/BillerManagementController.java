package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementRequest;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.service.BillerService;
import com.wayapay.thirdpartyintegrationservice.service.BillsPaymentService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;
import static com.wayapay.thirdpartyintegrationservice.util.Constants.SYNCED_IN_PROGRESS;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(API_V1+"/config/biller")
public class BillerManagementController {

    private final BillerService billerService;
    private final BillsPaymentService billsPaymentService;

    //createBiller
    @PostMapping
    public ResponseEntity<ResponseHelper> createBiller(@Valid @RequestBody BillerManagementRequest request){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(billerService.createBiller(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //UpdateBiller
    @PutMapping
    public ResponseEntity<ResponseHelper> updateBiller(@Valid @RequestBody BillerManagementRequest request){
        try {
            return ResponseEntity.ok(billerService.updateBiller(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getOne
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHelper> getById(@PathVariable String id){
        try {
            long billerId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(billerService.get(billerId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getAll
    @GetMapping
    public ResponseEntity<ResponseHelper> getAll(){
        try {
            return ResponseEntity.ok(billerService.getAll());
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //toggle
    @PutMapping("/toggle/{id}")
    public ResponseEntity<ResponseHelper> toggle(@PathVariable String id){
        try {
            long billerId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(billerService.toggle(billerId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //SyncBillers
    @PutMapping("/sync")
    public ResponseEntity<ResponseHelper> sync(){
        try {
            billsPaymentService.syncBiller();
            return ResponseEntity.ok(new SuccessResponse(SYNCED_IN_PROGRESS));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

}
