package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.CategoryManagementRequest;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.service.BillsPaymentService;
import com.wayapay.thirdpartyintegrationservice.service.CategoryService;
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
@RequestMapping(API_V1+"/config/category")
public class CategoryManageController {

    private final CategoryService categoryService;
    private final BillsPaymentService billsPaymentService;

    //createCategory
    @PostMapping
    public ResponseEntity<ResponseHelper> createCategory(@Valid @RequestBody CategoryManagementRequest request){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //UpdateCategory
    @PutMapping
    public ResponseEntity<ResponseHelper> updateCategory(@Valid @RequestBody CategoryManagementRequest request){
        try {
            return ResponseEntity.ok(categoryService.updateCategory(request));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getOne
    @GetMapping("/{id}")
    public ResponseEntity<ResponseHelper> getById(@PathVariable String id){
        try {
            long categoryId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(categoryService.get(categoryId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //getAll
    @GetMapping
    public ResponseEntity<ResponseHelper> getAll(){
        try {
            return ResponseEntity.ok(categoryService.getAll());
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //toggle
    @PutMapping("/toggle/{id}")
    public ResponseEntity<ResponseHelper> toggle(@PathVariable String id){
        try {
            long categoryId = CommonUtils.validateAndFetchIdAsLong(id);
            return ResponseEntity.ok(categoryService.toggle(categoryId));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    //SyncCategory
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
