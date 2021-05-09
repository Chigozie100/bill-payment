package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.service.ThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(API_V1+"/config/aggregator")
public class AggregatorManagementController {

    private final ThirdPartyService thirdPartyService;

    //getAll Aggregators
    @GetMapping
    public ResponseEntity<ResponseHelper> getAll(){
        try {
            return ResponseEntity.ok(thirdPartyService.getAll());
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getOne Aggregator
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHelper> getById(@PathVariable String id){
        try {
            long thirdPartyId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(thirdPartyService.get(thirdPartyId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //toggle Aggregator
    @PutMapping("/toggle/{id}")
    public ResponseEntity<ResponseHelper> toggle(@PathVariable String id){
        try {
            long thirdPartyId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(thirdPartyService.toggle(thirdPartyId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //sync Aggregator
    @PutMapping("/sync")
    public ResponseEntity<ResponseHelper> sync(){
        try {
            return ResponseEntity.ok(thirdPartyService.syncAggregator());
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

}
