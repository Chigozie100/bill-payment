package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeRequest;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.sample.response.SampleBillerFeeManagementResponse;
import com.wayapay.thirdpartyintegrationservice.sample.response.SampleBillerManagementResponse;
import com.wayapay.thirdpartyintegrationservice.sample.response.SampleErrorResponse;
import com.wayapay.thirdpartyintegrationservice.sample.response.SampleListBillerFeeManagementResponse;
import com.wayapay.thirdpartyintegrationservice.service.BillerConsumerFeeService;
import io.swagger.annotations.*;
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
@Api(tags = "Manage Fee", description = "This is the controller containing all the api to manage fees which should be beared by the biller or consumer", position = 2)
@RequestMapping(API_V1+"/config/fee")
public class BillerConsumerFeeController {

    private final BillerConsumerFeeService billerConsumerFeeService;

    //create
    @ApiOperation(value = "Create fee : This API is used to create fee.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleBillerFeeManagementResponse.class),
            @ApiResponse(code = 201, message = "Successful", response = SampleBillerFeeManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PostMapping
    public ResponseEntity<ResponseHelper> create(@Valid @RequestBody BillerConsumerFeeRequest request){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(billerConsumerFeeService.create(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //update
    @ApiOperation(value = "Update fee : This API is used to update fee.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleBillerFeeManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping
    public ResponseEntity<ResponseHelper> update(@Valid @RequestBody BillerConsumerFeeRequest request){
        try {
            return ResponseEntity.ok(billerConsumerFeeService.update(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //findAll
    @ApiOperation(value = "Get all fee : This API is used to get all fee.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleListBillerFeeManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping
    public ResponseEntity<ResponseHelper> getAll(){
        return ResponseEntity.ok(billerConsumerFeeService.getAll());
    }

    //findById
    @ApiOperation(value = "Get fee By Id : This API is used to get fee detail by id.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleBillerFeeManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHelper> getById(@ApiParam(example = "1") @PathVariable Long id){
        try {
            return ResponseEntity.ok(billerConsumerFeeService.getById(id));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //toggle
    @ApiOperation(value = "Toggle fee Detail : This API is used to disable/enable or off/on fee detail by id.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleBillerFeeManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping("/toggle/{id}")
    public ResponseEntity<ResponseHelper> toggle(@ApiParam(example = "1") @PathVariable Long id){
        try {
            return ResponseEntity.ok(billerConsumerFeeService.toggle(id));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

}
