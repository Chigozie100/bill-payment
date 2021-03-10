package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeRequest;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.service.BillerConsumerFeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(API_V1+"/config/fee")
public class BillerConsumerFeeController {

    private final BillerConsumerFeeService billerConsumerFeeService;

    //create
    @PostMapping
    public ResponseEntity<ResponseHelper> create(@Valid @RequestBody BillerConsumerFeeRequest request){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(billerConsumerFeeService.create(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //update
    @PutMapping
    public ResponseEntity<ResponseHelper> update(@Valid @RequestBody BillerConsumerFeeRequest request){
        try {
            return ResponseEntity.ok(billerConsumerFeeService.update(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //findAll
    @GetMapping
    public ResponseEntity<ResponseHelper> getAll(){
        return ResponseEntity.ok(billerConsumerFeeService.getAll());
    }

    //findById
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHelper> getById(@PathVariable Long id){
        try {
            return ResponseEntity.ok(billerConsumerFeeService.getById(id));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //toggle
    @PutMapping("/toggle/{id}")
    public ResponseEntity<ResponseHelper> toggle(@PathVariable Long id){
        try {
            return ResponseEntity.ok(billerConsumerFeeService.toggle(id));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

}
