package com.wayapay.thirdpartyintegrationservice.controller;


import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.CategoryDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.CreateChargeDto;
import com.wayapay.thirdpartyintegrationservice.v2.dto.request.UpdateServiceProvider;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.service.AdminBillPaymentService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;


@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@Api(tags = "Admin Bills Payment Api", description = "This is the main controller containing all the api to process admin billspayment")
@RequestMapping(path = API_V1 + "/admin")
@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
public class AdminController {

    private final AdminBillPaymentService adminBillPaymentService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch all bill category by admin", notes = "Fetch categories")
    @GetMapping(path = "/fetchAllCategory")
    public ResponseEntity<?> fetchAllCategory(HttpServletRequest request){
        ApiResponse<?> response =  adminBillPaymentService.fetchAllCategory(request.getHeader("authorization"));
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Activate bill category by admin", notes = "Activate bill category")
    @PatchMapping(path = "/activateCategory/{id}")
    public ResponseEntity<?> activateCategory(HttpServletRequest request,@PathVariable Long id,@RequestParam boolean isActive){
        ApiResponse<?> response =  adminBillPaymentService.activateCategory(request.getHeader("authorization"),id,isActive);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create bill category by admin", notes = "Create bill category")
    @PostMapping(path = "/createCategory")
    public ResponseEntity<?> createCategory(HttpServletRequest request, @RequestParam BillCategoryName name, @RequestParam String description){
        ApiResponse<?> response =  adminBillPaymentService.createCategory(request.getHeader("authorization"),name,description);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create bill service provider by admin", notes = "Create bill service provider")
    @PostMapping(path = "/createServiceProvider")
    public ResponseEntity<?> createServiceProvider(HttpServletRequest request, @Valid@RequestBody CategoryDto categoryDto){
        ApiResponse<?> response =  adminBillPaymentService.createServiceProvider(request.getHeader("authorization"),categoryDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Activate bill service provider by admin", notes = "Activate bill service provider")
    @PatchMapping(path = "/activateServiceProvider/{id}")
    public ResponseEntity<?> activateServiceProvider(HttpServletRequest request, @PathVariable Long id,@RequestParam boolean isActive){
        ApiResponse<?> response =  adminBillPaymentService.activateServiceProvider(request.getHeader("authorization"),id,isActive);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Update bill service provider by admin", notes = "Update bill service provider")
    @PutMapping(path = "/updateServiceProvider/{id}")
    public ResponseEntity<?> activateServiceProvider(HttpServletRequest request, @PathVariable Long id, @Valid@RequestBody UpdateServiceProvider updateServiceProvider){
        ApiResponse<?> response =  adminBillPaymentService.updateServiceProvider(request.getHeader("authorization"),updateServiceProvider,id);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch all bill service provider by admin", notes = "Fetch all bill service provider")
    @GetMapping(path = "/fetchAllServiceProvider")
    public ResponseEntity<?> fetchAllServiceProvider(HttpServletRequest request){
        ApiResponse<?> response =  adminBillPaymentService.fetchAllServiceProvider(request.getHeader("authorization"));
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Filter/Search all bill service provider by admin", notes = "Filter/Search all bill service provider")
    @GetMapping(path = "/filterAllServiceProvider")
    public ResponseEntity<?> filterAllServiceProvider(HttpServletRequest request,
                                                      @RequestParam(name = "pageNo",defaultValue = "1") int pageNo,
                                                      @RequestParam(name = "pageSize",defaultValue = "5") int pageSize){
        ApiResponse<?> response =  adminBillPaymentService.filterAllServiceProvider(request.getHeader("authorization"),pageNo,pageSize);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Filter/Search all bill category by admin", notes = "Filter/Search all bill category")
    @GetMapping(path = "/filterAllCategory")
    public ResponseEntity<?> filterAllCategory(HttpServletRequest request,
                                                      @RequestParam(name = "pageNo",defaultValue = "1") int pageNo,
                                                      @RequestParam(name = "pageSize",defaultValue = "5") int pageSize){
        ApiResponse<?> response =  adminBillPaymentService.filterAllCategory(request.getHeader("authorization"),pageNo,pageSize);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Filter/Search all bill service provider category by admin", notes = "Filter/Search all bill service provider category")
    @GetMapping(path = "/filterAllServiceProviderCategory")
    public ResponseEntity<?> filterAllServiceProviderCategory(HttpServletRequest request,
                                               @RequestParam(name = "pageNo",defaultValue = "1") int pageNo,
                                               @RequestParam(name = "pageSize",defaultValue = "5") int pageSize,
                                                              @RequestParam(name = "serviceProviderId",required = false) Long serviceProviderId){
        ApiResponse<?> response =  adminBillPaymentService.fetchServiceProviderCategory(request.getHeader("authorization"),serviceProviderId,pageNo,pageSize);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Filter/Search all bill service provider biller by admin", notes = "Filter/Search all bill service provider category")
    @GetMapping(path = "/filterAllServiceProviderBiller")
    public ResponseEntity<?> filterAllServiceProviderBiller(HttpServletRequest request,
                                                              @RequestParam(name = "pageNo",defaultValue = "1") int pageNo,
                                                              @RequestParam(name = "pageSize",defaultValue = "5") int pageSize,
                                                                    @RequestParam(value = "serviceProviderCategoryId",required = false) Long serviceProviderCategoryId){
        ApiResponse<?> response =  adminBillPaymentService.fetchServiceProviderBiller(request.getHeader("authorization"),serviceProviderCategoryId,pageNo,pageSize);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Filter/Search all bill service provider product by admin", notes = "Filter/Search all bill service provider product")
    @GetMapping(path = "/filterAllServiceProviderProduct")
    public ResponseEntity<?> filterAllServiceProviderProduct(HttpServletRequest request,
                                                                    @RequestParam(name = "pageNo",defaultValue = "1") int pageNo,
                                                                    @RequestParam(name = "pageSize",defaultValue = "5") int pageSize,
                                                                    @RequestParam(value = "serviceProviderBillerId",required = false) Long serviceProviderBillerId){
        ApiResponse<?> response =  adminBillPaymentService.fetchServiceProviderProduct(request.getHeader("authorization"),serviceProviderBillerId,pageNo,pageSize);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Filter/Search all bill service provider product bundle by admin", notes = "Filter/Search all bill service provider product bundle")
    @GetMapping(path = "/filterAllServiceProviderProductBundle")
    public ResponseEntity<?> filterAllServiceProviderProductBundle(HttpServletRequest request,
                                                             @RequestParam(name = "pageNo",defaultValue = "1") int pageNo,
                                                             @RequestParam(name = "pageSize",defaultValue = "5") int pageSize,
                                                             @RequestParam(value = "serviceProviderProductId",required = false) Long serviceProviderProductId){
        ApiResponse<?> response =  adminBillPaymentService.fetchServiceProviderProductBundle(request.getHeader("authorization"),serviceProviderProductId,pageNo,pageSize);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create bill service provider category by admin", notes = "Create bill service provider category")
    @PostMapping(path = "/createServiceProviderCategory/{serviceProviderId}")
    public ResponseEntity<?> createServiceProviderCategory(HttpServletRequest request,
                                                           @PathVariable Long serviceProviderId,
                                                           @RequestParam String name,
                                                           @RequestParam String serviceType,
                                                           @RequestParam String description){
        ApiResponse<?> response =  adminBillPaymentService.createServiceProviderCategory(request.getHeader("authorization"),
                serviceProviderId,name,description,serviceType);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }



    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create bill service provider category biller by admin", notes = "Create bill service provider category biller")
    @PostMapping(path = "/createServiceProviderBiller/{serviceProviderCategoryId}")
    public ResponseEntity<?> createServiceProviderBiller(HttpServletRequest request,
                                                           @PathVariable Long serviceProviderCategoryId,
                                                           @RequestParam String name,
                                                           @RequestParam String serviceType,
                                                           @RequestParam String description){
        ApiResponse<?> response =  adminBillPaymentService.createServiceProviderBiller(request.getHeader("authorization"),
                serviceProviderCategoryId,name,description,serviceType);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create bill service provider product by admin", notes = "Create bill service provider product")
    @PostMapping(path = "/createServiceProviderProduct/{serviceProviderBillerId}")
    public ResponseEntity<?> createServiceProviderProduct(HttpServletRequest request,
                                                         @PathVariable Long serviceProviderBillerId,
                                                         @RequestParam String name,
                                                         @RequestParam String serviceType,
                                                         @RequestParam String description,
                                                          @RequestParam boolean hasBundle,
                                                          @RequestParam boolean hasTokenValidation){
        ApiResponse<?> response =  adminBillPaymentService.createServiceProviderProduct(request.getHeader("authorization"),
                serviceProviderBillerId,name,description,serviceType,hasBundle,hasTokenValidation);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create bill service provider product bundle by admin", notes = "Create bill service provider product bundle")
    @PostMapping(path = "/createServiceProviderProductBundle/{serviceProviderProductId}")
    public ResponseEntity<?> createServiceProviderProductBundle(HttpServletRequest request,
                                                          @PathVariable Long serviceProviderProductId,
                                                          @RequestParam String name,
                                                          @RequestParam String serviceType,
                                                          @RequestParam String description,
                                                          @RequestParam BigDecimal amount){
        ApiResponse<?> response =  adminBillPaymentService.createServiceProviderProductBundle(request.getHeader("authorization"),
                serviceProviderProductId, amount,name,description,serviceType);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create bill service provider charges by admin", notes = "Create bill service provider charges")
    @PostMapping(path = "/createServiceProviderCharges")
    public ResponseEntity<?> createServiceProviderCharges(HttpServletRequest request,
                                                          @Valid@RequestBody CreateChargeDto createChargeDto,
                                                          @RequestParam Long serviceProviderId,
                                                          @RequestParam Long serviceProviderCategoryId){
        ApiResponse<?> response =  adminBillPaymentService.createBillProviderCharges(request.getHeader("authorization"),serviceProviderId,serviceProviderCategoryId,createChargeDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "update bill service provider charges by admin", notes = "update bill service provider charges")
    @PostMapping(path = "/updateServiceProviderCharges/{id}")
    public ResponseEntity<?> updateServiceProviderCharges(HttpServletRequest request,
                                                          @Valid@RequestBody CreateChargeDto createChargeDto,
                                                          @PathVariable Long id){
        ApiResponse<?> response =  adminBillPaymentService.updateBillProviderCharges(request.getHeader("authorization"),id,createChargeDto);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Update biller by admin", notes = "Update biller by admin")
    @PatchMapping(path = "/disableOrEnableBiller/{id}")
    public ResponseEntity<?> updateServiceProviderBiller(HttpServletRequest request, @PathVariable Long id,@RequestParam boolean isActive){
        ApiResponse<?> response =  adminBillPaymentService.updateBiller(request.getHeader("authorization"),id,isActive);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Update biller product by admin", notes = "Update biller product")
    @PatchMapping(path = "/disableOrEnableBillerProduct/{id}")
    public ResponseEntity<?> updateServiceProviderBillerProduct(HttpServletRequest request, @PathVariable Long id,@RequestParam(name = "amount",required = false) BigDecimal amount,@RequestParam boolean isActive){
        ApiResponse<?> response =  adminBillPaymentService.updateBillerProduct(request.getHeader("authorization"),id,amount,isActive);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Update biller product bundle by admin", notes = "Update biller product bundle")
    @PatchMapping(path = "/disableOrEnableBillerProductBundle/{id}")
    public ResponseEntity<?> updateServiceProviderBillerProductBundle(HttpServletRequest request, @PathVariable Long id,@RequestParam(name = "amount",required = false) BigDecimal amount,@RequestParam boolean isActive){
        ApiResponse<?> response =  adminBillPaymentService.updateBillerProductBundle(request.getHeader("authorization"),id,amount,isActive);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Update biller category by admin", notes = "Update biller category")
    @PatchMapping(path = "/disableOrEnableBillerCategory/{id}")
    public ResponseEntity<?> updateServiceProviderBillerCategory(HttpServletRequest request, @PathVariable Long id,@RequestParam boolean isActive){
        ApiResponse<?> response =  adminBillPaymentService.updateBillerCategory(request.getHeader("authorization"),id,isActive);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Update biller category by admin", notes = "Update biller category")
    @PatchMapping(path = "/disableOrEnableCategory/{id}")
    public ResponseEntity<?> disableOrEnableCategory(HttpServletRequest request, @PathVariable Long id,@RequestParam boolean isActive){
        ApiResponse<?> response =  adminBillPaymentService.updateCategory(request.getHeader("authorization"),id,isActive);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

}
