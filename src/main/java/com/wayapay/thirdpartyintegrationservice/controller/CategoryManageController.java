package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.CategoryManagementRequest;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.sample.response.*;
import com.wayapay.thirdpartyintegrationservice.service.BillsPaymentService;
import com.wayapay.thirdpartyintegrationservice.service.CategoryService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import io.swagger.annotations.*;
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
@Api(tags = "Manage Categories", description = "This is the controller containing all the api to manage category")
@RequestMapping(API_V1+"/config/category")
public class CategoryManageController {

    private final CategoryService categoryService;
    private final BillsPaymentService billsPaymentService;

    //createCategory
    @ApiOperation(value = "Create Category : This API is used to create category.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleCategoryManagementResponse.class),
            @ApiResponse(code = 201, message = "Successful", response = SampleCategoryManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PostMapping
    public ResponseEntity<ResponseHelper> createCategory(@Valid @RequestBody CategoryManagementRequest request){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //UpdateCategory
    @ApiOperation(value = "Update Category : This API is used to update category.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleCategoryManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping
    public ResponseEntity<ResponseHelper> updateCategory(@Valid @RequestBody CategoryManagementRequest request){
        try {
            return ResponseEntity.ok(categoryService.updateCategory(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getOne
    @ApiOperation(value = "Get Category By Id : This API is used to get category by providing id.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleCategoryManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHelper> getById(@ApiParam(example = "1") @PathVariable String id){
        try {
            long categoryId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(categoryService.get(categoryId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getAll
    @ApiOperation(value = "Get All Categories : This API is used to get all categories.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleListCategoryManagementResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping
    public ResponseEntity<ResponseHelper> getAll(){
        try {
            return ResponseEntity.ok(categoryService.getAll());
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //toggle
    @ApiOperation(value = "Toggle Category : This API is used to enable/diable or on/off a category.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleToggleCategoryResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping("/toggle/{id}")
    public ResponseEntity<ResponseHelper> toggle(@ApiParam(example = "1") @PathVariable String id){
        try {
            long categoryId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(categoryService.toggle(categoryId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //SyncCategory
    @ApiOperation(value = "Sync Category : This API is used to ensure that category from All aggregators are saved in the Application database.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleSyncInProgressResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PutMapping("/sync")
    public ResponseEntity<ResponseHelper> sync(){
        try {
            billsPaymentService.syncCategory();
            return ResponseEntity.ok(new SuccessResponse(SYNCED_IN_PROGRESS));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

}

