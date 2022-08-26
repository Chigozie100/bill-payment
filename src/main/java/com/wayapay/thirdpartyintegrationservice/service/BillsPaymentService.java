package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.config.ProfileDetailsService;
import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.Biller;
import com.wayapay.thirdpartyintegrationservice.model.Category;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.model.ThirdParty;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.service.auth.AuthFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.auth.UserDetail;
import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.service.commission.MerchantCommissionTrackerDto;
import com.wayapay.thirdpartyintegrationservice.service.dispute.DisputeService;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.service.itex.ItexService;
import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;
import com.wayapay.thirdpartyintegrationservice.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.ERROR_MESSAGE;
import static com.wayapay.thirdpartyintegrationservice.util.Constants.SYNCED_SUCCESSFULLY;

@Slf4j
@RequiredArgsConstructor
@Service
public class BillsPaymentService {

    private final ItexService itexService;
    private final BaxiService baxiService;
    private final QuickTellerService quickTellerService;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final DisputeService disputeService;
    private final OperationService operationService;
    private final BillerConsumerFeeService billerConsumerFeeService;
    private final CommissionOperationService commissionOperationService;
    private final NotificationService notificationService;

    private final CategoryService categoryService;
    private final BillerService billerService;
    private final ThirdPartyService thirdPartyService;
    private final AuthFeignClient authFeignClient;
    private final ProfileDetailsService profileDetailsService;

    //getAllCategories
    public List<CategoryResponse> getAllCategories() throws ThirdPartyIntegrationException {
        return categoryService.getAllActiveCategories();
    }

    //getBiller
    public List<BillerResponse> getAllBillers(String categoryId) throws ThirdPartyIntegrationException {
        return billerService.getAllActiveBillers(categoryId);
    }

    public IThirdPartyService getBillsPaymentService(String categoryId) throws ThirdPartyIntegrationException {
        return getBillsPaymentService(categoryService.findThirdPartyByCategoryAggregatorCode(categoryId).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Unknown Category id provided")));
    }

    public IThirdPartyService getBillsPaymentService(ThirdPartyNames thirdPartyName) {

        switch (thirdPartyName){
            case ITEX:
                return itexService;
            case BAXI:
                return baxiService;
            default:
            case QUICKTELLER:
                return quickTellerService;
        }
    }

    @Async
    public void syncCategory() throws ThirdPartyIntegrationException {
        //get all thirdParty/Aggregator
        //foreach thirdParty, get category
        //fetch all categories by aggregatorid from database >> ListFromDB
        //get all category from Aggregator >> listFromAPI
        //deleteAll category from ListFromDB but not in ListFromAPI
        //InsertAll category from ListFromAPI but not in ListFromDB

        try {
            List<ThirdParty> allAggregator = thirdPartyService.findAll();
            for (ThirdParty thirdParty : allAggregator) {

                //category from DB
                List<Category> categoryListFromDB = categoryService.findAllByAggregator(thirdParty.getId());

                //category from API
                List<CategoryResponse> categoryResponseListFromApi;
                try {
                    categoryResponseListFromApi = getBillsPaymentService(thirdParty.getThirdPartyNames()).getCategory();
                } catch (ThirdPartyIntegrationException e) {
                    log.error("Unable to fetch all categories from {}", thirdParty.getThirdPartyNames(), e);
                    continue;
                }

                if (categoryResponseListFromApi.isEmpty()){
                    log.error("No category returned from aggregator => {}", thirdParty.getThirdPartyNames());
                    continue;
                }

                List<String> categoryCodesFromAPI = categoryResponseListFromApi.stream().map(CategoryResponse::getCategoryId).collect(Collectors.toList());
                List<Category> categoryListNotInAPI = categoryListFromDB.stream().filter(category -> !categoryCodesFromAPI.contains(category.getCategoryAggregatorCode())).collect(Collectors.toList());
                categoryService.deleteAll(categoryListNotInAPI);

                List<String> categoryCodesFromDB = categoryListFromDB.stream().map(Category::getCategoryAggregatorCode).collect(Collectors.toList());
                List<Category> newCategoryListToBeSaved = categoryResponseListFromApi.stream().filter(categoryResponse -> !categoryCodesFromDB.contains(categoryResponse.getCategoryId())).map(categoryResponse -> new Category(categoryResponse.getCategoryName(), categoryResponse.getCategoryId(), thirdParty)).collect(Collectors.toList());
                categoryService.saveAll(newCategoryListToBeSaved);

            }
            log.info(SYNCED_SUCCESSFULLY);
        } catch (Exception exception) {
            log.error("Unable to sync categories", exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, ERROR_MESSAGE);
        }
    }

    @Async
    public void syncBiller() throws ThirdPartyIntegrationException {

        //get all categories and its thirdParty
        //foreach category, get All biller
        //fetch all billers by categoryId from database >> ListFromDB
        //get all billers by category from its Aggregator >> listFromAPI
        //deleteAll billers from ListFromDB but not in ListFromAPI
        //InsertAll billers from ListFromAPI but not in ListFromDB

        try {
            List<Category> categoryList = categoryService.findAll();
            for (Category category : categoryList) {

                //billers from DB
                List<Biller> billerListFromDB = billerService.findAllByCategoryId(category.getId());

                //biller from API
                List<BillerResponse> billerResponseListFromApi;
                try {
                    billerResponseListFromApi = getBillsPaymentService(category.getThirdParty().getThirdPartyNames()).getAllBillersByCategory(category.getCategoryAggregatorCode());

                } catch (ThirdPartyIntegrationException e) {
                    log.error("Unable to fetch all billers from {}", category.getThirdParty().getThirdPartyNames(), e);
                    continue;
                }

                if (billerResponseListFromApi.isEmpty()){
                    log.error("No biller returned by category => {} from aggregator => {}", category.getCategoryAggregatorCode(), category.getThirdParty().getThirdPartyNames());
                    continue;
                }

                List<String> billerCodesFromAPI = billerResponseListFromApi.stream().map(BillerResponse::getBillerId).collect(Collectors.toList());
                List<Biller> billerListNotInAPI = billerListFromDB.stream().filter(biller -> !billerCodesFromAPI.contains(biller.getBillerAggregatorCode())).collect(Collectors.toList());
                billerService.deleteAll(billerListNotInAPI);

                List<String> billerCodesFromDB = billerListFromDB.stream().map(Biller::getBillerAggregatorCode).collect(Collectors.toList());
                List<Biller> newBillerListToBeSaved = billerResponseListFromApi.stream().filter(billerResponse -> !billerCodesFromDB.contains(billerResponse.getBillerId())).map(billerResponse -> new Biller(billerResponse.getBillerName(), billerResponse.getBillerId(), category)).collect(Collectors.toList());
                billerService.saveAll(newBillerListToBeSaved);
            }

            log.info(SYNCED_SUCCESSFULLY);
        } catch (Exception exception) {
            log.error("Unable to sync biller", exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, ERROR_MESSAGE);
        }
    }


    private String getCategoryName(String category){
         if (category.equalsIgnoreCase("Airtime")){
             return TransactionCategory.AIRTIME_TOPUP.name();
         }else if (category.equalsIgnoreCase("insurance")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("Electricity")){
             return TransactionCategory.UTILITY.name();
         }
         else if (category.equalsIgnoreCase("Data")){
             return TransactionCategory.DATA_TOPUP.name();
         }else if (category.equalsIgnoreCase("CableTv")){
             return TransactionCategory.CABLE.name();
         }else if (category.equalsIgnoreCase("Internet")){
             return TransactionCategory.DATA_TOPUP.name();
         }else if (category.equalsIgnoreCase("Remita")){
             return TransactionCategory.TRANSFER.name();
         }else if (category.equalsIgnoreCase("LCC")){
             return TransactionCategory.AIRTIME_TOPUP.name();
         }else if (category.equalsIgnoreCase("databundle")){
             return TransactionCategory.DATA_TOPUP.name();
         }else if (category.equalsIgnoreCase("cabletv")){
             return TransactionCategory.CABLE.name();
         }else if (category.equalsIgnoreCase("vehiclepaper")){
             return TransactionCategory.TRANSFER.name();
         }else if (category.equalsIgnoreCase("epin")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("1")){
             return TransactionCategory.UTILITY.name();
         }
         else if (category.equalsIgnoreCase("2")){
             return TransactionCategory.CABLE.name();
         }
         else if (category.equalsIgnoreCase("3")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("4")){
             return TransactionCategory.AIRTIME_TOPUP.name();
         }
         else if (category.equalsIgnoreCase("7")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("8")){
             return TransactionCategory.AIRTIME_TOPUP.name();
         }
         else if (category.equalsIgnoreCase("9")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("10")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("11")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("12")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("13")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("14")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("15")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("16")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("17")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("18")){
             return TransactionCategory.TRANSFER.name();
         }else if (category.equalsIgnoreCase("19")){
             return TransactionCategory.TRANSFER.name();
         }else if (category.equalsIgnoreCase("20")){
             return TransactionCategory.TRANSFER.name();
         }else if (category.equalsIgnoreCase("21")){
             return TransactionCategory.TRANSFER.name();
         }
         else if (category.equalsIgnoreCase("22")){
             return TransactionCategory.TRANSFER.name();
         }else if (category.equalsIgnoreCase("23")){
             return TransactionCategory.TRANSFER.name();
         }else if (category.equalsIgnoreCase("24")){
             return TransactionCategory.TRANSFER.name();
         }else if (category.equalsIgnoreCase("25")){
             return TransactionCategory.TRANSFER.name();
         }else if (category.equalsIgnoreCase("26")){
             return TransactionCategory.TRANSFER.name();
         }else{ return TransactionCategory.TRANSFER.name();}

    }

    public PaymentResponse processPaymentOnBehalfOfUser(PaymentRequest paymentRequest, String userName,String token) throws ThirdPartyIntegrationException {
        UserProfileResponse userProfileResponse = operationService.getUserProfile(userName,token);
        //secure Payment
        String transactionId = String.valueOf(CommonUtils.generatePaymentTransactionId());

        String billType = getCategoryName(paymentRequest.getCategoryId());

        ThirdPartyNames thirdPartyName = categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        BigDecimal fee = billerConsumerFeeService.getFee(paymentRequest.getAmount(), thirdPartyName, paymentRequest.getBillerId());
        FeeBearer feeBearer = billerConsumerFeeService.getFeeBearer(thirdPartyName, paymentRequest.getBillerId());
        if (operationService.secureFundAdmin(paymentRequest.getAmount(), fee, userName, paymentRequest.getSourceWalletAccountNumber(), transactionId, feeBearer, token, billType)){
            try {
                PaymentResponse paymentResponse = getBillsPaymentService(paymentRequest.getCategoryId()).processPayment(paymentRequest, fee, transactionId, userName);
                //store the transaction information
                PaymentTransactionDetail paymentTransactionDetail = operationService.saveTransactionDetail(userProfileResponse,paymentRequest, fee, paymentResponse, userName, transactionId);

                CompletableFuture.runAsync(() -> {
                    try {
                        String phoneNumber = extractPhone(paymentRequest);
                        userProfileResponse.setPhoneNumber(phoneNumber);
                        notificationService.pushSMS(paymentTransactionDetail, token, paymentResponse, userProfileResponse);

                    } catch (ThirdPartyIntegrationException e) {
                        e.printStackTrace();
                    }
                });

                UserDetail userDetail = profileDetailsService.getUser(token);
                if (userDetail.getCorporate()){
                    CompletableFuture.runAsync(() -> {
                        try {
                            getCommissionForMakingBillsPayment(userDetail, userName,token, paymentRequest.getAmount());
                        } catch (ThirdPartyIntegrationException e) {
                            e.printStackTrace();
                        }
                    });

                    CompletableFuture.runAsync(() -> {
                        try {
                            calculateMerchantPercentage(userDetail, paymentRequest.getBillerId(), userName, token,paymentRequest.getAmount());
                        } catch (ThirdPartyIntegrationException e) {
                            e.printStackTrace();
                        }
                    });
                }

                Map<String,String> map = new HashMap<>();
                map.put("message", "Making Bills Payment");
                map.put("userId", userName);
                map.put("module", "Bills Payment");
                CompletableFuture.runAsync(() -> {
                    try {
                        operationService.logUserActivity(paymentRequest, map, token);
                    } catch (ThirdPartyIntegrationException e) {
                        e.printStackTrace();
                    }
                });

                return paymentResponse;
            } catch (ThirdPartyIntegrationException e) {
                log.info("Error in billspayment :: " +e.getMessage());
                operationService.saveFailedTransactionDetail(userProfileResponse,paymentRequest, fee, null, userName, transactionId);

                disputeService.logTransactionAsDispute(userName, paymentRequest, thirdPartyName, paymentRequest.getBillerId(), paymentRequest.getCategoryId(), paymentRequest.getAmount(), fee, transactionId);
                throw new ThirdPartyIntegrationException(e.getHttpStatus(), e.getMessage());
            }
        }

        log.error("Unable to secure fund from user's wallet");
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }



    public PaymentResponse processPayment(PaymentRequest paymentRequest, String userName, String token) throws ThirdPartyIntegrationException {
        UserProfileResponse userProfileResponse = operationService.getUserProfile(userName,token);
        //secure Payment
        String transactionId = String.valueOf(CommonUtils.generatePaymentTransactionId());

        String billType = getCategoryName(paymentRequest.getCategoryId());

        ThirdPartyNames thirdPartyName = categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        BigDecimal fee = billerConsumerFeeService.getFee(paymentRequest.getAmount(), thirdPartyName, paymentRequest.getBillerId());
        FeeBearer feeBearer = billerConsumerFeeService.getFeeBearer(thirdPartyName, paymentRequest.getBillerId());
        if (operationService.secureFund(paymentRequest.getAmount(), fee, userName, paymentRequest.getSourceWalletAccountNumber(), transactionId, feeBearer, token, billType)){
            try {
                PaymentResponse paymentResponse = getBillsPaymentService(paymentRequest.getCategoryId()).processPayment(paymentRequest, fee, transactionId, userName);
                //store the transaction information
                PaymentTransactionDetail paymentTransactionDetail = operationService.saveTransactionDetail(userProfileResponse,paymentRequest, fee, paymentResponse, userName, transactionId);

                // call the receipt service
//                CompletableFuture.runAsync(() -> {
//                    try {
//                        notificationService.pushINAPP2(userName,paymentTransactionDetail,paymentResponse,token);
//                    } catch (ThirdPartyIntegrationException e) {
//                        e.printStackTrace();
//                    }
//                });

//                CompletableFuture.runAsync(() -> {
//                    try {
//                        Map<String, String> map = emailMessageBuilder(paymentResponse,paymentTransactionDetail,userProfileResponse);
//                        notificationService.pushEMAIL(map,token);
//                    } catch (ThirdPartyIntegrationException e) {
//                        e.printStackTrace();
//                    }
//                });
                CompletableFuture.runAsync(() -> {
                    try {
                        String phoneNumber = extractPhone(paymentRequest);
                        userProfileResponse.setPhoneNumber(phoneNumber);
                        notificationService.pushSMS(paymentTransactionDetail, token, paymentResponse, userProfileResponse);

                    } catch (ThirdPartyIntegrationException e) {
                        e.printStackTrace();
                    }
                });
//
                UserDetail userDetail = profileDetailsService.getUser(token);
                if (userDetail.getCorporate()){
                    CompletableFuture.runAsync(() -> {
                        try {
                            getCommissionForMakingBillsPayment(userDetail, userName,token, paymentRequest.getAmount());
                        } catch (ThirdPartyIntegrationException e) {
                            e.printStackTrace();
                        }
                    });

                    CompletableFuture.runAsync(() -> {
                        try {
                            calculateMerchantPercentage(userDetail, paymentRequest.getBillerId(), userName, token,paymentRequest.getAmount());
                        } catch (ThirdPartyIntegrationException e) {
                            e.printStackTrace();
                        }
                    });
                }

                Map<String,String> map = new HashMap<>();
                map.put("message", "Making Bills Payment");
                map.put("userId", userName);
                map.put("module", "Bills Payment");
                CompletableFuture.runAsync(() -> {
                    try {
                        operationService.logUserActivity(paymentRequest, map, token);
                    } catch (ThirdPartyIntegrationException e) {
                        e.printStackTrace();
                    }
                });

                return paymentResponse;
            } catch (ThirdPartyIntegrationException e) {
                log.info("Error in billspayment :: " +e.getMessage());
                operationService.saveFailedTransactionDetail(userProfileResponse,paymentRequest, fee, null, userName, transactionId);

                disputeService.logTransactionAsDispute(userName, paymentRequest, thirdPartyName, paymentRequest.getBillerId(), paymentRequest.getCategoryId(), paymentRequest.getAmount(), fee, transactionId);
                throw new ThirdPartyIntegrationException(e.getHttpStatus(), e.getMessage());
            }
        }

        log.error("Unable to secure fund from user's wallet");
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    private String extractPhone(PaymentRequest paymentRequest){
        List<ParamNameValue> listValue = paymentRequest.getData();
        String phoneNumber = null;
        for (ParamNameValue paramNameValue : listValue) {
            ParamNameValue value = new ParamNameValue();
            value.setName(paramNameValue.getName());
            value.setValue(paramNameValue.getValue());
            if (paramNameValue.getName().equalsIgnoreCase("phone")) {
                phoneNumber = paramNameValue.getValue();
            }
        }
        return Objects.requireNonNull(phoneNumber).substring(1);
    }

    Map<String, String> inAppMessageBuilder(PaymentResponse paymentResponse, PaymentTransactionDetail paymentTransactionDetail, String transactionId){

        Map<String, String> map = new HashMap<>();
        map.put("message", "Making Bulk Bills Payment");
        map.put("userId", "userName");
        map.put("module", "Bills Payment");

        return map;

    }


//    private String extractData(PaymentResponse paymentResponse, PaymentTransactionDetail paymentTransactionDetail){
//        List<ParamNameValue> listValue = paymentResponse.getData();
//        String message = null;
//        for (int i = 0; i < listValue.size(); i++) {
//            ParamNameValue value = new ParamNameValue();
//            value.setName(listValue.get(i).getName());
//            value.setValue(listValue.get(i).getValue());
//            message = "Your account has "+ "\n" +
//                    ""+"been credited with:" +paymentTransactionDetail.getAmount() +" \n" +
//                    "" + value.getValue();
//        }
//        return message;
//    }



//    public PaymentResponse processMultiplePayment(MultiplePaymentRequest paymentRequest, String userName, String token) throws ThirdPartyIntegrationException {
//        // get user profile
//        UserProfileResponse userProfileResponse = operationService.getUserProfile(userName,token);
//                //secure Payment
//        String transactionId = String.valueOf(CommonUtils.generatePaymentTransactionId());
//        String billType = getCategoryName(paymentRequest.getCategoryId());
//
//        ThirdPartyNames thirdPartyName = categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
//        BigDecimal fee = billerConsumerFeeService.getFee(paymentRequest.getAmount(), thirdPartyName, paymentRequest.getBillerId());
//        FeeBearer feeBearer = billerConsumerFeeService.getFeeBearer(thirdPartyName, paymentRequest.getBillerId());
//        if (operationService.secureFund(paymentRequest.getAmount(), fee, userName, paymentRequest.getSourceWalletAccountNumber(), transactionId, feeBearer, token, billType)) {
//            try {
//                PaymentResponse paymentResponse = getBillsPaymentService(paymentRequest.getCategoryId()).processMultiplePayment(paymentRequest, fee, transactionId, userName);
//                //store the transaction information
//                PaymentTransactionDetail paymentTransactionDetail = operationService.saveTransactionDetailMultiple(userProfileResponse, paymentRequest, fee, paymentResponse, userName, transactionId);
//                // notify customer
//                pushINAPP(paymentTransactionDetail, token, paymentResponse);
//                pushEMAIL(paymentTransactionDetail, token, paymentResponse, userProfileResponse);
//
//                /**
//                 * check user type
//                 * get commission for merchant user
//                 * credit the merchant user's commission wallet
//                 */
//
//
//                //logTransaction(paymentRequest,paymentResponse,token,userName);
//                Map<String, String> map = new HashMap<>();
//                map.put("message", "Making Bulk Bills Payment");
//                map.put("userId", userName);
//                map.put("module", "Bills Payment");
//                CompletableFuture.runAsync(() -> {
//                    try {
//                        operationService.logUserActivity(paymentRequest, map, token);
//                    } catch (ThirdPartyIntegrationException e) {
//                        e.printStackTrace();
//                    }
//                });
//
//                return paymentResponse;
//            } catch (ThirdPartyIntegrationException e) {
//                log.error("This is the error from payment :::: " + e.getMessage());
//                disputeService.logTransactionAsDispute(userName, paymentRequest, thirdPartyName, paymentRequest.getBillerId(), paymentRequest.getCategoryId(), paymentRequest.getAmount(), fee, transactionId);
//
//                throw new ThirdPartyIntegrationException(e.getHttpStatus(), e.getMessage());
//            }
//        }
//
//        log.error("Unable to secure fund from user's wallet");
//        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
//    }


    public ResponseEntity<?> processBulkPayment(MultipartFile file,String token) throws ThirdPartyIntegrationException, IOException {
        List<PaymentResponse> paymentResponse = null;
        if (ExcelHelper.hasExcelFormat(file)) {
            paymentResponse = buildBulkPayment(ExcelHelper.excelToPaymentRequest(file.getInputStream(),
                    file.getOriginalFilename()), token);
        }

        // build payment request
        return new ResponseEntity<>(new SuccessResponse(paymentResponse), HttpStatus.OK);
    }

    List<PaymentResponse> buildBulkPayment(BulkBillsPaymentDTO bulkBillsPaymentDTO,String token) throws ThirdPartyIntegrationException {
        List<PaymentResponse> paymentResponseList = new ArrayList<>();

        PaymentRequest paymentRequest = new PaymentRequest();

        for (PaymentRequestExcel mPayUser : bulkBillsPaymentDTO.getPaymentRequestExcels()) {

            paymentRequest.setSourceWalletAccountNumber(mPayUser.getSourceWalletAccountNumber());
            paymentRequest.setAmount(BigDecimal.valueOf(mPayUser.getAmount()));
            paymentRequest.setCategoryId(mPayUser.getCategoryId());
            paymentRequest.setBillerId(mPayUser.getBillerId());

            List<ParamNameValue> data = new ArrayList<>();

            data.add(new ParamNameValue("phone",mPayUser.getPhone()));
            data.add(new ParamNameValue("amount",mPayUser.getAmount().toString()));
            data.add(new ParamNameValue("paymentMethod",mPayUser.getPaymentMethod()));
            data.add(new ParamNameValue("channel",mPayUser.getChannel()));
            data.add(new ParamNameValue("plan",mPayUser.getPlan()));


            paymentRequest.setData(data);

            PaymentResponse paymentResponse = processPayment(paymentRequest, mPayUser.getUserId(), token);
            paymentResponseList.add(paymentResponse);
        }

        return paymentResponseList;
    }

    public ResponseEntity<?> processBulkPaymentForm(List<MultiplePaymentRequest>  multipleFormPaymentRequest, String token) throws ThirdPartyIntegrationException {

        PaymentResponse paymentResponse = null;

        for (MultiplePaymentRequest request : multipleFormPaymentRequest) {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(request.getAmount());
            paymentRequest.setCategoryId(request.getCategoryId());
            paymentRequest.setBillerId(request.getBillerId());
            paymentRequest.setData(request.getData());
            paymentResponse = processPayment(paymentRequest, request.getUsername(), token);

        }
        // build payment request
        return new ResponseEntity<>(new SuccessResponse(paymentResponse), HttpStatus.OK);
    }


    public Map<String, Object> search(String username, int pageNumber, int pageSize){
        Pageable paging = getPageable(pageNumber, pageSize);
        Page<TransactionDetail> transactionDetailPage;
        List<TransactionDetail> transactionDetailList;

        if (CommonUtils.isEmpty(username)){
            transactionDetailPage = paymentTransactionRepo.getAllTransaction(paging);
            transactionDetailList = transactionDetailPage.getContent();
            return getTransactionMap(transactionDetailList,transactionDetailPage);
        }
        transactionDetailPage = paymentTransactionRepo.getAllTransactionByUsername(username, paging);

        transactionDetailList = transactionDetailPage.getContent();

        return getTransactionMap(transactionDetailList,transactionDetailPage);
    }


    public Map<String, Object> searchByReferralCode(String referralCode, int page, int size){
        Pageable paging = getPageable(page, size);
        Page<TransactionDetail> transactionDetailPage;
        List<TransactionDetail> transactionDetailList;

        if (CommonUtils.isEmpty(referralCode)){
            transactionDetailPage = paymentTransactionRepo.getAllTransaction(paging);
            transactionDetailList = transactionDetailPage.getContent();
            return getTransactionMap(transactionDetailList,transactionDetailPage);
        }

        transactionDetailPage = paymentTransactionRepo.getAllTransactionByReferralCode(referralCode, paging);

        transactionDetailList = transactionDetailPage.getContent();

        List<TransactionDetail> transactionDetailPage2 = paymentTransactionRepo.getAllTransactionByReferralCodeGroupedBy(referralCode);



        for (int i = 0; i < transactionDetailPage2.size()-1; i++)
        {

            for (int j = i+1; j < transactionDetailPage2.size(); j++)
            {
//                log.info("Second ::: {} ");
//                System.out.println("i  " + transactionDetailPage2.get(i).getUsername());

                //&& (transactionDetailPage2.get(i) != transactionDetailPage2.get(j))
                if (transactionDetailPage2.get(i).getUsername().equalsIgnoreCase(transactionDetailPage2.get(j).getUsername()) && (i != j))
                {


                    System.out.println("Duplicate Element transactionDetailPage2.get(i) : "+transactionDetailPage2.get(j));
                }
            }
        }



        return getTransactionMap(transactionDetailList,transactionDetailPage);
    }

    private Pageable getPageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    private Map<String, Object> getTransactionMap(List<TransactionDetail> transactionDetailList, Page<TransactionDetail> transactionDetailPage){
        Map<String, Object> response = new HashMap<>();

        response.put("transactionList", transactionDetailList);
        response.put("currentPage", transactionDetailPage.getNumber());
        response.put("totalItems", transactionDetailPage.getTotalElements());
        response.put("totalPages", transactionDetailPage.getTotalPages());
        return response;
    }


    //    findByUsername
    public long findByUsername(String username) throws ThirdPartyIntegrationException {
        if (CommonUtils.isEmpty(username)){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, "NOT Found");
        }
        return paymentTransactionRepo.findByUsername(username);
    }


    public Page<TransactionDetail> searchAndFilterTransactionStatus(boolean status, int pageNumber, int pageSize){
        return paymentTransactionRepo.getAllTransactionBySuccessful(status,PageRequest.of(pageNumber, pageSize));
    }

    public Page<TransactionDetail> searchByAccountType(String userAccountNumber, int pageNumber, int pageSize){
        if (CommonUtils.isEmpty(userAccountNumber)){
            return paymentTransactionRepo.getAllTransaction(PageRequest.of(pageNumber, pageSize));
        }

        return paymentTransactionRepo.getAllTransactionByUserAccountNumber(userAccountNumber,PageRequest.of(pageNumber, pageSize));
    }


    //    // as a merchant user i should be able to receive certain % amount commission anytime i use my waya app to make bilspayment
    public void getCommissionForMakingBillsPayment(UserDetail userDetail, String userId, String token, BigDecimal amount) throws ThirdPartyIntegrationException {

        String userType = getUserType(userDetail, userId,token);

        if (userType !=null){
            MerchantCommissionTrackerDto trackerDto= new MerchantCommissionTrackerDto();
            trackerDto.setUserId(userId);
            trackerDto.setUserType(UserType.valueOf(userType));
            trackerDto.setCommissionType(CommissionType.PERCENTAGE);
            trackerDto.setCommissionValue(BigDecimal.ONE.doubleValue());
            trackerDto.setTransactionType(TransactionType.BILLS_PAYMENT);
            commissionOperationService.saveMerchantCommission(trackerDto,token);

            payCommissionToMerchant(UserType.valueOf(userType),userId,token, amount);  // pay user commission

        }

    }

    private UserProfileResponsePojo getUserProfile(String userId, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ApiResponseBody<UserProfileResponsePojo>> userProfile = authFeignClient.getUserByUserId(userId, token);
            ApiResponseBody<UserProfileResponsePojo> responseBody = userProfile.getBody();
            return Objects.requireNonNull(responseBody).getData();
        }catch (Exception e){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }
    private String getUserType(UserDetail userDetail, String userId, String token) throws ThirdPartyIntegrationException {
      //  UserProfileResponsePojo userProfile =  getUserProfile(userId, token);
        for(String role : userDetail.getRoles()) {
            if(UserType.ROLE_CORP.name().equalsIgnoreCase(role)
                    || UserType.ROLE_CORP_ADMIN.name().equalsIgnoreCase(role)
                    || UserType.ROLE_AGENT.name().equalsIgnoreCase(role)
                    || UserType.ROLE_AGGREGATOR.name().equalsIgnoreCase(role)
                    || UserType.ROLE_MERCHANT.name().equalsIgnoreCase(role)){
                return role;
            }
        }
        return null;

    }


    public void payCommissionToMerchant(UserType userType,String userName, String token, BigDecimal amount) throws ThirdPartyIntegrationException {
        commissionOperationService.payUserCommission(userType,userName,token, amount); // log commission
    }

    //as a merchant user anytime i sell billspayment a certain % amount of the item sold amount is transferred on real time to my commission wallet from WAYA
    private void calculateMerchantPercentage(UserDetail userDetail, String billerId, String userName, String token, BigDecimal amount) throws ThirdPartyIntegrationException {
        String userType = getUserType(userDetail,userName,token);
        commissionOperationService.payOrganisationCommission(UserType.valueOf(userType),billerId,userName,token, amount); // log commission
       // 1. get the biller  2. find the biller commission 3. find the corporate user Id 4. compute the Percetage 5. credit the commission wallet
    }


    //check for the userType who's Item has been purchased
//    public ThirdParty checkCustomerWhosItemIsBeanPurchased(){
//        ThirdParty thirdParty = new ThirdParty();
//        return thirdParty;
//    }

    //ABILITY for waya admin with the right access and permission to select which of the waya official account to make the billspayment from
    public List<NewWalletResponse> adminSelectWayaOfficialAccount(String token) throws ThirdPartyIntegrationException {
        return operationService.getWayaOfficialWallet(token);
    }


//    private  Object processAdminPayment(PaymentRequest paymentRequest, String userId){
//        // use the userID to get Profile of the user
//        //
//
//    }


}