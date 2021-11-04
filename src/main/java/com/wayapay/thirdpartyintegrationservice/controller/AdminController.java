package com.wayapay.thirdpartyintegrationservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.TransactionTracker;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.service.BillsPaymentService;
import com.wayapay.thirdpartyintegrationservice.service.IThirdPartyService;
import com.wayapay.thirdpartyintegrationservice.service.OperationService;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.service.referral.ReferralCodePojo;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.TransferPojo;
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
import java.util.Map;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@Api(tags = "Admin Bills Payment API", description = "This is the main controller containing all the api to process admin billspayment")
@RequestMapping(API_V1)
public class AdminController {

    private final BillsPaymentService billsPaymentService;
    private final OperationService operationService;
    private final QuickTellerService iThirdPartyService;

//    @ApiOperation(value = "Get Transaction Report : This API is used to get all transaction report", position = 8)
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful")
//    })
//    @GetMapping("/biller/report/transaction/{transaction_id}")
//    public ResponseEntity<ResponseHelper> getTransactionReportByTransactionId(@PathVariable String transaction_id) throws ThirdPartyIntegrationException {
//
//        TransactionDetail transactionDetailPage = billsPaymentService.searchTransactionByTransactionID(transaction_id);
//        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
//    }

    // faild transactions
    // get all failed transaction
    // get all successful transaction

    @ApiOperation(value = "Get Transaction Status Report : This API is used to get all transaction report faild or successfull", position = 9)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/biller/report/filter/{transaction_status}")
    public ResponseEntity<ResponseHelper> searchAndFilterTransactionStatus(@PathVariable("transaction_status") boolean transaction_status, @RequestParam(required = false, defaultValue = "0") String pageNumber, @RequestParam(required = false, defaultValue = "10") String pageSize ) throws ThirdPartyIntegrationException {

        Page<TransactionDetail> transactionDetailPage = billsPaymentService.searchAndFilterTransactionStatus(transaction_status,Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
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
    @GetMapping("/admin/report/user/{userId}")
    public ResponseEntity<ResponseHelper> adminSearchByUserID(@PathVariable("userId") String userId, @RequestParam(required = false, defaultValue = "0") String pageNumber, @RequestParam(required = false, defaultValue = "10") String pageSize ) throws ThirdPartyIntegrationException {

        Map<String, Object> transactionDetailPage = billsPaymentService.search(userId,Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    //ABILITY for waya admin with the right access and permission to make bills payment to users - signle
    @ApiOperation(value = "Admin Get Users Transaction Report : This API is used to get all transaction report by user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @PostMapping("/admin/make-payment/{userId}")
    public ResponseEntity<ResponseHelper> adminMakeBillsPaymentToUser(@Valid @RequestBody PaymentRequest paymentRequest, @ApiIgnore @RequestAttribute(Constants.TOKEN) String token, @PathVariable String userId) throws ThirdPartyIntegrationException, URISyntaxException {

        PaymentResponse transactionDetailPage = billsPaymentService.processPayment(paymentRequest, userId, token);
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    //ability for waya admin to manage and reverse all billspayment of the users
    @ApiOperation(value = "Admin Get Users Transaction Report : This API is used to get all transaction report by user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @PostMapping("/admin/make-reversal-payment/{userId}")
    public ResponseEntity<ResponseHelper> adminMakeReversalPaymentToUser(@Valid @RequestBody PaymentRequest paymentRequest, @PathVariable String userId, @ApiIgnore @RequestAttribute(Constants.TOKEN) String token){

        PaymentResponse transactionDetailPage = null;
        //operationService.adminMakeReversalPayment(paymentRequest, userId, token);
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }

    //ABILITY for waya admin with the right access and permission to make bills payment to users - multiple webform
    @ApiOperation(value = "Bulk Bills Payment: This API is used to by the admin to pay bills on behalf of users using web form", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/admin/bulk-user-bills-payment")
    public ResponseEntity<?> makeBulkBillsPaymentToUsersWithWebForm(@Valid @RequestBody List<MultiplePaymentRequest> multipleFormPaymentRequest, @ApiIgnore @RequestAttribute(Constants.TOKEN) String token) throws ThirdPartyIntegrationException, URISyntaxException {
        return billsPaymentService.processBulkPaymentForm(multipleFormPaymentRequest, token);
    }

    @ApiOperation(value = "Bulk Bills Payment", tags = {"ADMIN"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Response Headers")})
    @PostMapping(path = "/admin/bulk-user-excel", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> makeBulkBillsPaymentToUsers(@RequestParam("file") MultipartFile file,
                                                         HttpServletRequest request, @ApiIgnore @RequestAttribute(Constants.TOKEN) String token) throws ThirdPartyIntegrationException, IOException, URISyntaxException {
        return billsPaymentService.processBulkPayment(file, request, token);
    }

    @ApiOperation(value = "Admin Get Users Transaction Count : This API is used to get all transaction report by user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/admin/get-transaction-count/{username}")
    public ResponseEntity<Long> getTransactionCount(@PathVariable String username) throws ThirdPartyIntegrationException {

        long transactionDetailPage = billsPaymentService.findByUsername(username);
        return ResponseEntity.ok(transactionDetailPage);
    }


    @ApiOperation(value = "Admin Refund Failed Transaction to users : This API is used to refund failed transactions to users")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @PostMapping("/admin/refund-failed-transaction")
    public ResponseEntity<ResponseHelper> refundFailedTransaction(@Valid @RequestBody TransferFromOfficialToMainWallet transfer, @ApiIgnore @RequestAttribute(Constants.TOKEN) String token) throws ThirdPartyIntegrationException {

        List<WalletTransactionPojo> transactionDetailPage = operationService.refundFailedTransaction(transfer, token);
        return ResponseEntity.ok(new SuccessResponse(transactionDetailPage));
    }


    @GetMapping("/getCategories")
    public ResponseEntity<ResponseHelper> getInterswitchCategories() throws ThirdPartyIntegrationException {
         return ResponseEntity.ok(new SuccessResponse(iThirdPartyService.getCategory()));
    }

    @GetMapping("/getBillersByCategories/{category_id}")
    public ResponseEntity<ResponseHelper> getAllInterswitchBillersByCategory(@PathVariable String category_id) throws ThirdPartyIntegrationException {
        return ResponseEntity.ok(new SuccessResponse(iThirdPartyService.getAllBillersByCategory(category_id)));
    }

    @GetMapping("/getBillersByCategories/{category_id}/biller/{biller_id}")
    public ResponseEntity<ResponseHelper> getCustomerValidationFormByBiller(@PathVariable String category_id, @PathVariable String biller_id) throws ThirdPartyIntegrationException {
        return ResponseEntity.ok(new SuccessResponse(iThirdPartyService.getCustomerValidationFormByBiller(category_id, biller_id)));
    }


    @ApiOperation(value = "Admin Refund Failed Transaction to users : This API is used to refund failed transactions to users")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @PostMapping("/admin/test-tracker")
    public ResponseEntity<ResponseHelper> testTracker(@Valid @RequestBody TransactionTracker transfer, @ApiIgnore @RequestAttribute(Constants.TOKEN) String token) throws ThirdPartyIntegrationException {
        TransactionTracker transactionTracker = operationService.testTracker(transfer);
        return ResponseEntity.ok(new SuccessResponse(transactionTracker));
    }

    @ApiOperation(value = "Test : Test view referralCode")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/admin/test-tracker/{referralCode}")
    public ResponseEntity<ResponseHelper> getReferralDetails(@PathVariable String referralCode,@ApiIgnore @RequestAttribute(Constants.TOKEN) String token) throws ThirdPartyIntegrationException {
        ReferralCodePojo transactionTracker = operationService.getReferralDetails(referralCode,token);
        return ResponseEntity.ok(new SuccessResponse(transactionTracker));
    }


    @ApiOperation(value = "Admin Refund Failed Transaction to users : This API is used to refund failed transactions to users")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @PostMapping("/admin/test-push-kafka")
    public ResponseEntity<ResponseHelper> testPushWayaGramPayment(@Valid @RequestBody TransferPojo transferPojo) throws ThirdPartyIntegrationException, JsonProcessingException {
        operationService.testPushWayaGramPayment(transferPojo);
        return ResponseEntity.ok(new SuccessResponse(transferPojo));
    }









}
