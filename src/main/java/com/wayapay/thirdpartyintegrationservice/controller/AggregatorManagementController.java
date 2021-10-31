package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.sample.response.*;
import com.wayapay.thirdpartyintegrationservice.service.ThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@Api(tags = "Manage Aggregators", description = "This is the controller containing all the api to manager aggregators/third-parties like ITEX, BAXI, QUICKTELLER", position = 1)
@RequestMapping(API_V1+"/config/aggregator")
public class AggregatorManagementController {

    private final ThirdPartyService thirdPartyService;

    //getAll Aggregators
    @ApiOperation(value = "Get All Aggregators : This API is used to get all aggregators.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleListAggregatorResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping
    public ResponseEntity<ResponseHelper> getAll(){
        try {
            return ResponseEntity.ok(thirdPartyService.getAll());
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getOne Aggregator
    @ApiOperation(value = "Get Aggregator By Id : This API is used to get aggregator by providing an Id.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleAggregatorResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHelper> getById(@ApiParam(example = "1") @PathVariable String id){
        try {
            long thirdPartyId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(thirdPartyService.get(thirdPartyId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //toggle Aggregator
    @ApiOperation(value = "Toggle Aggregator By Id : This API is used to disable/enable or off/on aggregator status by providing an Id.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleToggleAggregatorResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping("/toggle/{id}")
    public ResponseEntity<ResponseHelper> toggle(@ApiParam(example = "1") @PathVariable String id){
        try {
            long thirdPartyId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(thirdPartyService.toggle(thirdPartyId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //sync Aggregator
    @ApiOperation(value = "sync Aggregator : This API is used to ensure that aggregator configured is same with that saved in the database")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleSyncResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping("/sync")
    public ResponseEntity<ResponseHelper> sync(){
        try {
            return ResponseEntity.ok(thirdPartyService.syncAggregator());
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

}
