package com.wayapay.thirdpartyintegrationservice.controller;

import com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.TransactionTracker;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.service.OperationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.API_V1;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@Api(tags = "Commission Operation ", description = "This is the controller containing all the api to Commission Operation")
@RequestMapping(API_V1+"/commission")
public class CommissionOperationController {

    private final OperationService operationService;

    @ApiOperation(value = "Get Transaction Report : This API is used to get all transaction count by referralCode", position = 8)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @GetMapping("/get-transactions-by-referralCode/{referralCode}")
    public ResponseEntity<ResponseHelper> getListOfTransactions(@PathVariable("referralCode") String referralCode) throws ThirdPartyIntegrationException {
        List<TransactionTracker> transactionTrackerList = operationService.getListOfTransactions(referralCode);
        return ResponseEntity.ok(new SuccessResponse(transactionTrackerList));
    }

    @ApiOperation(value = "Update Transaction status : This API is used to update transaction status of user that have been paid bonus", position = 8)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful")
    })
    @PutMapping("/update-transactions-status/{id}")
    public ResponseEntity<ResponseHelper> updateTransactionsStatus(@PathVariable("id") Long id) throws ThirdPartyIntegrationException {
        TransactionTracker transactionTrackerList = operationService.updateTransactionsStatus(id);
        return ResponseEntity.ok(new SuccessResponse(transactionTrackerList));
    }


}
