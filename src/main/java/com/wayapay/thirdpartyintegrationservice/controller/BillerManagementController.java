package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementRequest;
import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementResponse;
import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementResponseExtended;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseObj;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.sample.response.*;
import com.wayapay.thirdpartyintegrationservice.service.BillerService;
import com.wayapay.thirdpartyintegrationservice.service.BillsPaymentService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;
import static com.wayapay.thirdpartyintegrationservice.util.Constants.SYNCED_IN_PROGRESS;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@Api(tags = "Manage Biller", description = "This is the controller containing all the api to manage billers", position = 3)
@RequestMapping(API_V1+"/config/biller")
public class BillerManagementController {

    private final BillerService billerService;
    private final BillsPaymentService billsPaymentService;

    //createBiller
    @ApiOperation(value = "Create Biller : This API is used to create biller.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleBillerManagementResponse.class),
            @ApiResponse(code = 201, message = "Successful", response = SampleBillerManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PostMapping
    public ResponseEntity<ResponseHelper> createBiller(@Valid @RequestBody BillerManagementRequest request){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(billerService.createBiller(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //UpdateBiller
    @ApiOperation(value = "Update Biller : This API is used to update biller.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleBillerManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping
    public ResponseEntity<ResponseHelper> updateBiller(@Valid @RequestBody BillerManagementRequest request){
        try {
            return ResponseEntity.ok(billerService.updateBiller(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getOne
    @ApiOperation(value = "Get Biller : This API is used to get biller by the provided id.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleBillerManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHelper> getById(@ApiParam(example = "1") @PathVariable String id){
        try {
            long billerId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(billerService.get(billerId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getAll
    @ApiOperation(value = "Get All Billers : This API is used to get all billers.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleListBillerManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping
    public ResponseEntity<ResponseHelper> getAll(){
        try {
            return ResponseEntity.ok(billerService.getAll());
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //toggle
    @ApiOperation(value = "Toggle Biller : This API is used to enable/disable or on/off a biller.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleToggleBillerResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping("/toggle/{id}")
    public ResponseEntity<ResponseHelper> toggle(@ApiParam(example = "1") @PathVariable String id){
        try {
            long billerId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(billerService.toggle(billerId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //SyncBillers
    @ApiOperation(value = "Sync Biller : This API is used to ensure that billers on all aggregators are same as on the database.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleSyncInProgressResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping("/sync")
    public ResponseEntity<ResponseHelper> sync(){
        try {
            billsPaymentService.syncBiller();
            return ResponseEntity.ok(new SuccessResponse(SYNCED_IN_PROGRESS));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }


    @ApiOperation(value = "{ Don not use this endpoint}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleListBillerManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/sync-commission-billers")
    public ResponseEntity<ResponseObj<List<BillerManagementResponseExtended>>> getAllBiller() throws Exception {
        try {
            return billerService.getAllBillers();
        } catch (ThirdPartyIntegrationException e) {
           throw new Exception(e.getMessage());
        }
    }

}
