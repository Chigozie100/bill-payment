package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ErrorResponse;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.sample.response.*;
import com.wayapay.thirdpartyintegrationservice.service.BillsPaymentService;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@Api(tags = "Core Bills Payment API", description = "This is the main controller containing all the api to process billspayment")
@RequestMapping(API_V1)
public class BillsPaymentController {

    private final BillsPaymentService billsPaymentService;


    @ApiOperation(value = "Get Categories : This API is used to get all categories.", position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleListCategoryResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/category")
    public ResponseEntity<ResponseHelper> getAllCategories(){
        try {
            List<CategoryResponse> categoryResponseList = billsPaymentService.getAllCategories();
            return ResponseEntity.ok(new SuccessResponse(categoryResponseList));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @ApiOperation(value = "Get Billers Based on the selected Category : This API is used to get all Billers based on the selected category.", position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleListBillerResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ResponseHelper> getBillersByCategory(@ApiParam(example = "Airtime") @PathVariable("categoryId") String categoryId){
        try {
            List<BillerResponse> allBillersByCategory = billsPaymentService.getAllBillers(categoryId);
            return ResponseEntity.ok(new SuccessResponse(allBillersByCategory));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @ApiOperation(value = "Get Payment Items based on the selected biller : This API is used to get selected Biller's Payment Item", position = 3)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SamplePaymentItemsResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/category/{categoryId}/biller/{billerId}")
    public ResponseEntity<ResponseHelper> getBillerPaymentItem(@ApiParam(example = "Airtime") @PathVariable("categoryId") String categoryId, @ApiParam(example = "mtnvtu") @PathVariable("billerId") String billerId){
        try {
            PaymentItemsResponse customerValidationFormByBiller = billsPaymentService.getBillsPaymentService(categoryId).getCustomerValidationFormByBiller(categoryId, billerId);
            log.info("customerValidationFormByBiller ::: " + customerValidationFormByBiller);
            return ResponseEntity.ok(new SuccessResponse(customerValidationFormByBiller));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @ApiOperation(value = "Validate Provided Payment Item : This API is used to validate payment items value provided by customer", position = 4)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleCustomerValidationResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PostMapping("/biller/validate")
    public ResponseEntity<ResponseHelper> validateCustomerPaymentItem(@Valid @RequestBody CustomerValidationRequest customerValidationRequest){
        try {
            CustomerValidationResponse customerValidationResponse = billsPaymentService.getBillsPaymentService(customerValidationRequest.getCategoryId()).validateCustomerValidationFormByBiller(customerValidationRequest);
            return ResponseEntity.ok(new SuccessResponse(customerValidationResponse));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @ApiOperation(value = "Make Payment : This API is used to make payment such that customer gets value", position = 5)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SamplePaymentResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @PostMapping("/biller/pay")
    public ResponseEntity<ResponseHelper> customerPayment(@Valid @RequestBody PaymentRequest paymentRequest, @ApiIgnore @RequestAttribute(Constants.USERNAME) String username, @ApiIgnore @RequestAttribute(Constants.TOKEN) String token, Authentication authentication) throws URISyntaxException{
        try {

            PaymentResponse paymentResponse = billsPaymentService.processPayment(paymentRequest, username, token);
            return ResponseEntity.ok(new SuccessResponse(paymentResponse));
        } catch (ThirdPartyIntegrationException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    @ApiOperation(value = "Get Transaction Report : This API is used to get all transaction report", position = 6)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleListCategoryResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/biller/report")
    public ResponseEntity<ResponseHelper> customerTransactionReport(@RequestParam(required = false, defaultValue = "0") String pageNumber,  @RequestParam(required = false, defaultValue = "10") String pageSize, @ApiIgnore @RequestAttribute(Constants.USERNAME) String username){

        Map<String, Object> transactionDetailPage = billsPaymentService.search(username, Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    @ApiOperation(value = "Get Transaction Report By Referral Code : This API is used to get all transaction report by user referralCode", position =7)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = SampleListCategoryResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = SampleErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SampleErrorResponse.class)
    })
    @GetMapping("/biller/report/referral/{referralCode}")
    public ResponseEntity<ResponseHelper> customerTransactionReportByReferral(@PathVariable String referralCode, @RequestParam(required = false, defaultValue = "0") String pageNumber, @RequestParam(required = false, defaultValue = "10") String pageSize){

        Map<String, Object> transactionDetailPage = billsPaymentService.searchByReferralCode(referralCode, Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    @ApiOperation(value = "User Get Transaction Report By user Id: This API is used to get all transaction report by user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/biller/report/user/{userId}")
    public ResponseEntity<ResponseHelper> adminSearchByUserID(@PathVariable("userId") String userId, @RequestParam(required = false, defaultValue = "0") String pageNumber, @RequestParam(required = false, defaultValue = "10") String pageSize ) throws ThirdPartyIntegrationException {

        Map<String, Object> transactionDetailPage = billsPaymentService.search(userId,Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    

}