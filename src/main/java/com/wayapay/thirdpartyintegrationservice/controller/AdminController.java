package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.sample.response.SampleErrorResponse;
import com.wayapay.thirdpartyintegrationservice.sample.response.SampleListCategoryResponse;
import com.wayapay.thirdpartyintegrationservice.service.BillsPaymentService;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;

@Slf4j
@RequiredArgsConstructor
@RestController
@Api(tags = "Admin Bills Payment API", description = "This is the main controller containing all the api to process admin billspayment")
@RequestMapping(API_V1)
public class AdminController {

    private final BillsPaymentService billsPaymentService;




    @ApiOperation(value = "Get Transaction Report : This API is used to get all transaction report", position = 8)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/biller/report/transaction/{transaction_id}")
    public ResponseEntity<ResponseHelper> getTransactionReportByTransactionId(@PathVariable("transactionID") String transactionID) throws ThirdPartyIntegrationException {

        TransactionDetail transactionDetailPage = billsPaymentService.searchTransactionByTransactionID(transactionID);
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    // faild transactions
    // get all failed transaction
    // get all successful transaction

    @ApiOperation(value = "Get Transaction Status Report : This API is used to get all transaction report faild or successfull", position = 9)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/biller/report/filter/{transaction_status}")
    public ResponseEntity<ResponseHelper> searchAndFilterTransactionStatus(@PathVariable("transaction_type") Boolean transaction_type, @RequestParam(required = false, defaultValue = "0") String pageNumber, @RequestParam(required = false, defaultValue = "10") String pageSize ) throws ThirdPartyIntegrationException {

        Page<TransactionDetail> transactionDetailPage = billsPaymentService.searchAndFilterTransactionStatus(transaction_type,Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }


    // searchBy Account
    @ApiOperation(value = "Get Transaction Report By Account : This API is used to get all transaction report by Account", position = 10)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/biller/report/transaction/{account}")
    public ResponseEntity<ResponseHelper> searchByAccount(@PathVariable("account") String account,  @RequestParam(required = false, defaultValue = "0") String pageNumber,  @RequestParam(required = false, defaultValue = "10") String pageSize ) throws ThirdPartyIntegrationException {

        Page<TransactionDetail> transactionDetailPage = billsPaymentService.searchByAccountType(account,Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    // working well test: 30/07/2021
    @ApiOperation(value = "Admin Get Waya Official Account Transaction Report : This API is used to get all transaction report by user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/admin/get-waya-official-account")
    public ResponseEntity<ResponseHelper> adminSelectWayaOfficialAccount(@RequestHeader("Authorization") String token) throws ThirdPartyIntegrationException {
        List<NewWalletResponse> transactionDetailPage = billsPaymentService.adminSelectWayaOfficialAccount(token);
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }


    //ability for waya admin to display ,  report and extract all histroy of billspayment of the user and waya official in lowest level of details
    @ApiOperation(value = "Admin Get Users Transaction Report : This API is used to get all transaction report by user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/report/user/{userId}")
    public ResponseEntity<ResponseHelper> adminSearchByUserID(@PathVariable("userId") String userId, @RequestParam(required = false, defaultValue = "0") String pageNumber, @RequestParam(required = false, defaultValue = "10") String pageSize ) throws ThirdPartyIntegrationException {

        Page<TransactionDetail> transactionDetailPage = billsPaymentService.search(userId,Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    //ABILITY for waya admin with the right access and permission to make bills payment to users - signle
    @ApiOperation(value = "Admin Get Users Transaction Report : This API is used to get all transaction report by user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @PostMapping("/admin-make-payment")
    public ResponseEntity<ResponseHelper> adminMakeBillsPaymentToUser(@Valid @RequestBody PaymentRequest paymentRequest, @ApiIgnore @RequestAttribute(Constants.USERNAME) String username, @ApiIgnore @RequestAttribute(Constants.TOKEN) String token, Authentication authentication) throws ThirdPartyIntegrationException, URISyntaxException {

        PaymentResponse transactionDetailPage = billsPaymentService.processPayment(paymentRequest, username, token);
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    //ability for waya admin to manage and reverse all billspayment of the users
    @ApiOperation(value = "Admin Get Users Transaction Report : This API is used to get all transaction report by user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @PostMapping("/admin-make-reversal-payment/{userId}")
    public ResponseEntity<ResponseHelper> adminMakeReversalPaymentToUser(@Valid @RequestBody PaymentRequest paymentRequest, @PathVariable String userId, @ApiIgnore @RequestAttribute(Constants.TOKEN) String token) throws ThirdPartyIntegrationException, URISyntaxException {

        PaymentResponse transactionDetailPage = null;
        //operationService.adminMakeReversalPayment(paymentRequest, userId, token);
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

}
