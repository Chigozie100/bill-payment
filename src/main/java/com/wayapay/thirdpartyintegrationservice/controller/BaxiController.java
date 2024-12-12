package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.BaxiProxy;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto.DataBundleRequest;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto.DataBundleResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.*;


@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@Api(tags = "Admin Baxi Bills Payment Api", description = "This is the main controller containing all the api to process admin baxi bills payment")
@RequestMapping(path = API_V1 + "/baxi")
@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
public class BaxiController {

    private final BaxiService baxiService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create baxi service provider category by admin", notes = "Create baxi bill service provider category")
    @PostMapping(path = "/createCategory/{serviceProviderId}")
    public ResponseEntity<?> createServiceProviderCategories(HttpServletRequest request,
                                                           @PathVariable Long serviceProviderId){
        ApiResponse<?> response =  baxiService.createBaxiServiceProviderCategory(request,request.getHeader(HEADER_STRING),serviceProviderId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create baxi service provider biller by admin", notes = "Create baxi bill service provider biller")
    @PostMapping(path = "/createBiller/{serviceProviderId}")
    public ResponseEntity<?> createServiceProviderBillers(HttpServletRequest request,
                                                           @PathVariable Long serviceProviderId){
        ApiResponse<?> response =  baxiService.createBaxiServiceProviderBiller(request,request.getHeader(HEADER_STRING),serviceProviderId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create baxi service provider product by admin", notes = "Create baxi bill service provider product")
    @PostMapping(path = "/createProductAndBundle")
    public ResponseEntity<?> createServiceProviderProductAndBundle(HttpServletRequest request,
                                                         @RequestParam Long serviceProviderCategoryId,
                                                          @RequestParam Long serviceProviderId){
        ApiResponse<?> response =  baxiService.createBaxiServiceProviderProduct(request,request.getHeader(HEADER_STRING),serviceProviderId,serviceProviderCategoryId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create baxi service provider biller logo by admin", notes = "Create baxi bill service provider biller logo")
    @PostMapping(path = "/createBillerLogo")
    public ResponseEntity<?> createServiceProviderBillerLogo(HttpServletRequest request,
                                                          @RequestParam Long serviceProviderCategoryId,
                                                          @RequestParam Long serviceProviderId){
        ApiResponse<?> response =  baxiService.createBaxiServiceProviderBillerLogo(request,request.getHeader(HEADER_STRING),serviceProviderId,serviceProviderCategoryId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch bill transaction reference by admin", notes = "etch bill transaction reference")
    @GetMapping(path = "/queryTransaction/{reference}")
    public ResponseEntity<?> fetchBaxiTransactionQuery(HttpServletRequest request,
                                                             @RequestParam String reference){
        ApiResponse<?> response =  baxiService.fetchTransactionQuery(request,request.getHeader(HEADER_STRING),reference);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

}
