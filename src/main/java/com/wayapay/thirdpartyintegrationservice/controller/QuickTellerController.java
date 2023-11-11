package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.v2.dto.response.ApiResponse;
import com.wayapay.thirdpartyintegrationservice.v2.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto.SendPaymentAdviceRequest;
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

import javax.servlet.http.HttpServletRequest;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;
import static com.wayapay.thirdpartyintegrationservice.util.Constants.HEADER_STRING;


@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@Api(tags = "Admin QuickTeller Bills Payment Api", description = "This is the main controller containing all the api to process admin quickteller bills payment")
@RequestMapping(path = API_V1 + "/quickteller")
@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
public class QuickTellerController {

    private final QuickTellerService quickTellerService;


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create quickteller service provider category by admin", notes = "Create quickteller bill service provider category")
    @PostMapping(path = "/createCategory/{serviceProviderId}")
    public ResponseEntity<?> createQtServiceProviderCategory(HttpServletRequest request,
                                                           @PathVariable Long serviceProviderId){
        ApiResponse<?> response =  quickTellerService.createQuickTellerServiceProviderCategory(request,request.getHeader(HEADER_STRING),serviceProviderId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create quickteller service provider category by admin", notes = "Create quickteller bill service provider category")
    @PostMapping(path = "/createBillerByCategory/{serviceProviderId}")
    public ResponseEntity<?> createQtServiceProviderBillerByCategory(HttpServletRequest request,
                                                                   @PathVariable Long serviceProviderId,
                                                                   @RequestParam Long serviceProviderCategoryId){
        ApiResponse<?> response =  quickTellerService.createQuickTellerServiceProviderBiller(request,request.getHeader(HEADER_STRING),serviceProviderId,serviceProviderCategoryId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Create quickteller service provider product by biller by admin", notes = "Create quickteller bill service provider product by biller")
    @PostMapping(path = "/createProductByBiller/{serviceProviderId}")
    public ResponseEntity<?> createQtServiceProviderProductByBiller(HttpServletRequest request,
                                                                     @PathVariable Long serviceProviderId,
                                                                     @RequestParam Long serviceProviderCategoryId){
        ApiResponse<?> response =  quickTellerService.createQuickTellerServiceProviderProductByBiller(request,request.getHeader(HEADER_STRING),serviceProviderId,serviceProviderCategoryId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch auth credential by admin", notes = "Fetch auth credential ")
    @PostMapping(path = "/getAuth")
    public ResponseEntity<?> fetchQtAuthCredentials(HttpServletRequest request,
                                                             @RequestParam String url){
        ApiResponse<?> response =  quickTellerService.fetchHeaders(request,request.getHeader(HEADER_STRING),url);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch biller by category", notes = "Fetch biller by category")
    @GetMapping(path = "/getBiller/{categoryId}")
    public ResponseEntity<?> fetchQtBillers(HttpServletRequest request,
                                                @PathVariable String categoryId){
        ApiResponse<?> response =  quickTellerService.fetchBiller(request,request.getHeader(HEADER_STRING),categoryId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Fetch all category", notes = "Fetch all category")
    @GetMapping(path = "/getCategory")
    public ResponseEntity<?> fetchQtCategories(HttpServletRequest request){
        ApiResponse<?> response =  quickTellerService.fetchCategories(request,request.getHeader(HEADER_STRING));
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Get pay item by billerId", notes = "Get pay item by billerId")
    @GetMapping(path = "/getPayItem/{billerId}")
    public ResponseEntity<?> getQtPayItem(HttpServletRequest request, @PathVariable String billerId){
        ApiResponse<?> response =  quickTellerService.getPayItem(request,request.getHeader(HEADER_STRING),billerId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
    @ApiOperation(value = "Get transactionQuery", notes = "Get transactionQuery")
    @GetMapping(path = "/transactionQuery")
    public ResponseEntity<?> getQuickTellerTransactionQuery(HttpServletRequest request, @RequestParam String reference){
        ApiResponse<?> response =  quickTellerService.verifyQuickTellerTransactionRefAdmin(request,request.getHeader(HEADER_STRING),reference);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true, dataTypeClass = String.class)})
//    @ApiOperation(value = "make payment by admin", notes = "Make payment biller")
//    @PostMapping(path = "/payment/{categoryType}")
//    public ResponseEntity<?> makePayment(HttpServletRequest request,
//                                         @RequestBody SendPaymentAdviceRequest paymentAdviceRequest,
//                                         @PathVariable String categoryType){
//        ApiResponse<?> response =  quickTellerService.makeBillsPaymentRequest(paymentAdviceRequest,categoryType);
//        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
//    }


}
