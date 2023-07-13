package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.v2.dto.request.*;
import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.service.BillPaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.HEADER_STRING;
import static com.wayapay.thirdpartyintegrationservice.v2.dto.Constants.API_V1;

@RestController
@RequestMapping(path = API_V1)
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@Api(tags = "Customer Bills Payment Api", description = "This is the main controller containing all the api to process customer bill payment")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch all category", notes = "Fetch all category")
    @GetMapping(path = "/fetchAllCategory")
    public ResponseEntity<?> fetchAllCategories(HttpServletRequest request){
        ApiResponse<?> response =  billPaymentService.fetchAllBillCategory(request.getHeader(HEADER_STRING));
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch all billers by category", notes = "Get biller by category")
    @GetMapping(path = "/fetchBillerByCategory")
    public ResponseEntity<?> fetchAllBillerByCategory(HttpServletRequest request,
                                                   @RequestParam Long categoryId){
        ApiResponse<?> response =  billPaymentService.fetchBillersByCategory(request.getHeader(HEADER_STRING),categoryId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch all product by biller", notes = "Get product by biller")
    @GetMapping(path = "/fetchProductByBiller")
    public ResponseEntity<?> fetchAllProductByBiller(HttpServletRequest request,
                                                   @RequestParam Long serviceProviderBillerId){
        ApiResponse<?> response =  billPaymentService.fetchAllProductByBiller(request.getHeader(HEADER_STRING),serviceProviderBillerId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch all bundle by product", notes = "Get bundle by product")
    @GetMapping(path = "/fetchBundleByProduct")
    public ResponseEntity<?> fetchAllBundleByProduct(HttpServletRequest request,
                                                     @RequestParam Long serviceProviderProductId){
        ApiResponse<?> response =  billPaymentService.fetchAllBundleByProduct(request.getHeader(HEADER_STRING),serviceProviderProductId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
//    @ApiOperation(value = "Fetch all product ADDONS by productCode, planName", notes = "Get ADDONs by product")
//    @GetMapping(path = "/fetchAddOnsByProduct")
//    public ResponseEntity<?> fetchAddOnsByProduct(HttpServletRequest request,
//                                                     @RequestParam Long serviceProviderProductId,
//                                                  @RequestParam(name = "productCode", required = false) String productCode){
//        ApiResponse<?> response =  billPaymentService.fetchAllAddOnsByProduct(request.getHeader(HEADER_STRING),serviceProviderProductId,productCode);
//        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
//    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Verify customer smartCard,meterNumber,betting account, etc", notes = "Verify customer smartCard,meterNumber,betting account, etc")
    @PostMapping(path = "/verifyCustomerToken")
    public ResponseEntity<?> verifyCustomerToken(HttpServletRequest request,
                                                 @Valid @RequestBody ValidateCustomerToken validateCustomerToken){
        ApiResponse<?> response =  billPaymentService.verifyCustomerAccountOrToken(request.getHeader(HEADER_STRING),validateCustomerToken);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Make Electricity payment", notes = "Make electricity payment")
    @PostMapping(path = "/electricity/pay")
    public ResponseEntity<?> makeElectricityPayment(HttpServletRequest request,
                                                 @Valid @RequestBody ElectricityPaymentDto electricityPaymentDto,
                                                    @RequestHeader("pin") String pin,
                                                    @RequestParam String userAccountNumber,
                                                    @RequestParam Long serviceProviderId,
                                                    @RequestParam Long serviceProviderBillerId){
        ApiResponse<?> response =  billPaymentService.makeElectricityPayment(request.getHeader(HEADER_STRING),
                serviceProviderBillerId,serviceProviderId,electricityPaymentDto,userAccountNumber,pin);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Make Airtime payment", notes = "Make Airtime payment")
    @PostMapping(path = "/airtime/pay")
    public ResponseEntity<?> makeAirtimePayment(HttpServletRequest request,
                                                    @Valid @RequestBody AirtimePaymentDto airtimePaymentDto,
                                                    @RequestHeader("pin") String pin,
                                                    @RequestParam String userAccountNumber,
                                                    @RequestParam Long serviceProviderId,
                                                    @RequestParam Long serviceProviderBillerId){
        ApiResponse<?> response =  billPaymentService.makeAirtimePayment(request.getHeader(HEADER_STRING),
                serviceProviderBillerId,serviceProviderId,airtimePaymentDto,userAccountNumber,pin);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Make Data bundle payment", notes = "Make Data bundle payment")
    @PostMapping(path = "/dataBundle/pay")
    public ResponseEntity<?> makeDataBundlePayment(HttpServletRequest request,
                                                @Valid @RequestBody DataBundlePaymentDto dataBundlePaymentDto,
                                                @RequestHeader("pin") String pin,
                                                @RequestParam String userAccountNumber,
                                                @RequestParam Long serviceProviderId,
                                                @RequestParam(name = "serviceProviderBundleId",required = false) Long serviceProviderBundleId,
                                                   @RequestParam(name = "serviceProviderBillerId",required = false) Long serviceProviderBillerId){
        ApiResponse<?> response =  billPaymentService.makeDataBundlePayment(request.getHeader(HEADER_STRING),
                serviceProviderBundleId,serviceProviderId,dataBundlePaymentDto,userAccountNumber,pin,serviceProviderBillerId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Make Cable Tv payment", notes = "Make Cable Tv payment")
    @PostMapping(path = "/cableTv/pay")
    public ResponseEntity<?> makeCableTvPayment(HttpServletRequest request,
                                                   @Valid @RequestBody CableTvPaymentDto cableTvPaymentDto,
                                                   @RequestHeader("pin") String pin,
                                                   @RequestParam String userAccountNumber,
                                                   @RequestParam Long serviceProviderId,
                                                   @RequestParam(name = "serviceProviderBundleId",required = false) Long serviceProviderBundleId,
                                                   @RequestParam(name = "serviceProviderBillerId",required = false) Long serviceProviderBillerId){
        ApiResponse<?> response =  billPaymentService.makeCableTvPayment(request.getHeader(HEADER_STRING),
                serviceProviderBundleId,serviceProviderId,cableTvPaymentDto,userAccountNumber,pin,serviceProviderBillerId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Make Epin payment", notes = "Make Epin payment")
    @PostMapping(path = "/epin/pay")
    public ResponseEntity<?> makeEpinPayment(HttpServletRequest request,
                                                @Valid @RequestBody EpinPaymentDto epinPaymentDto,
                                                @RequestHeader("pin") String pin,
                                                @RequestParam String userAccountNumber,
                                                @RequestParam Long serviceProviderId,
                                                @RequestParam(name = "serviceProviderBundleId",required = false) Long serviceProviderBundleId,
                                                @RequestParam(name = "serviceProviderBillerId",required = false) Long serviceProviderBillerId){
        ApiResponse<?> response =  billPaymentService.makeEpinPayment(request.getHeader(HEADER_STRING),
                serviceProviderBundleId,serviceProviderId,epinPaymentDto,userAccountNumber,pin,serviceProviderBillerId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }



    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Make Epin payment", notes = "Make Epin payment")
    @PostMapping(path = "/betting/pay")
    public ResponseEntity<?> makeBettingPayment(HttpServletRequest request,
                                             @Valid @RequestBody BettingPaymentDto bettingPaymentDto,
                                             @RequestHeader("pin") String pin,
                                             @RequestParam String userAccountNumber,
                                             @RequestParam Long serviceProviderId,
                                             @RequestParam Long serviceProviderBillerId){
        ApiResponse<?> response =  billPaymentService.makeBettingPayment(request.getHeader(HEADER_STRING),
                serviceProviderBillerId,serviceProviderId,bettingPaymentDto,userAccountNumber,pin);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Make other bills payment", notes = "Make other bills payment")
    @PostMapping(path = "/other/payment")
    public ResponseEntity<?> makeOtherPayment(HttpServletRequest request,
                                                  @Valid @RequestBody OthersPaymentDto othersPaymentDto,
                                                  @RequestHeader("pin") String pin,
                                                  @RequestParam String userAccountNumber,
                                                  @RequestParam Long serviceProviderId,
                                                  @RequestParam Long serviceProviderBillerId){
        ApiResponse<?> response =  billPaymentService.makeOtherPayment(request.getHeader(HEADER_STRING),
                serviceProviderBillerId,serviceProviderId,othersPaymentDto,userAccountNumber,pin);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch transaction by reference number", notes = "Get transaction by reference number")
    @GetMapping(path = "/fetchTransactionByReference/{reference}")
    public ResponseEntity<?> fetchTransactionByReference(HttpServletRequest request,
                                                      @RequestParam String reference){
        ApiResponse<?> response =  billPaymentService.fetchBillTransactionByReference(request.getHeader(HEADER_STRING),reference);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

}
