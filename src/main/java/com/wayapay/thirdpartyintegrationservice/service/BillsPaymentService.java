package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.Biller;
import com.wayapay.thirdpartyintegrationservice.model.Category;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import com.wayapay.thirdpartyintegrationservice.model.ThirdParty;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.service.auth.AuthFeignClient;
import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.service.commission.MerchantCommissionTrackerDto;
import com.wayapay.thirdpartyintegrationservice.service.dispute.DisputeService;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.service.itex.ItexService;
import com.wayapay.thirdpartyintegrationservice.service.notification.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    public void adminProcessPayment(PaymentRequest paymentRequest, String userName, String token) throws ThirdPartyIntegrationException {

    }

        public PaymentResponse processPayment(PaymentRequest paymentRequest, String userName, String token) throws ThirdPartyIntegrationException {

        UserProfileResponse userProfileResponse = operationService.getUserProfile(userName,token);

        //secure Payment
        String transactionId = CommonUtils.generatePaymentTransactionId();

        ThirdPartyNames thirdPartyName = categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        BigDecimal fee = billerConsumerFeeService.getFee(paymentRequest.getAmount(), thirdPartyName, paymentRequest.getBillerId());
        FeeBearer feeBearer = billerConsumerFeeService.getFeeBearer(thirdPartyName, paymentRequest.getBillerId());
        if (operationService.secureFund(paymentRequest.getAmount(), fee, userName, paymentRequest.getSourceWalletAccountNumber(), transactionId, feeBearer, token)){
            try {
                PaymentResponse paymentResponse = getBillsPaymentService(paymentRequest.getCategoryId()).processPayment(paymentRequest, fee, transactionId, userName);
                //store the transaction information
                PaymentTransactionDetail paymentTransactionDetail = operationService.saveTransactionDetail(userProfileResponse,paymentRequest, fee, paymentResponse, userName, transactionId);

                // call the receipt service
                CompletableFuture.runAsync(() -> {
                    try {
                        notificationService.pushINAPP(paymentTransactionDetail,token,paymentResponse);
                    } catch (ThirdPartyIntegrationException e) {
                        e.printStackTrace();
                    }
                });
                CompletableFuture.runAsync(() -> {
                    try {
                        notificationService.pushEMAIL(paymentTransactionDetail,token,paymentResponse, userProfileResponse);
                    } catch (ThirdPartyIntegrationException e) {
                        e.printStackTrace();
                    }
                });
                CompletableFuture.runAsync(() -> {
                    try {
                        log.info("paymentTransactionDetail :: " +paymentTransactionDetail);
                        log.info("paymentResponse :: " +paymentResponse);
                        log.info("userProfileResponse :: " +userProfileResponse);
                       notificationService.pushSMS(paymentTransactionDetail, token, paymentResponse, userProfileResponse);

                    } catch (ThirdPartyIntegrationException e) {
                        e.printStackTrace();
                    }
                });

                getCommissionForMakingBillsPayment(userName,token, paymentRequest.getAmount());

//                CompletableFuture.runAsync(() -> {
//                    try {
//
//                    } catch (ThirdPartyIntegrationException e) {
//                        e.printStackTrace();
//                    }
//                });


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
                operationService.saveFailedTransactionDetail(userProfileResponse,paymentRequest, fee, null, userName, null);

                disputeService.logTransactionAsDispute(userName, paymentRequest, thirdPartyName, paymentRequest.getBillerId(), paymentRequest.getCategoryId(), paymentRequest.getAmount(), fee, transactionId);
                throw new ThirdPartyIntegrationException(e.getHttpStatus(), e.getMessage());
            }
        }

        log.error("Unable to secure fund from user's wallet");
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    public PaymentResponse processMultiplePayment(MultiplePaymentRequest paymentRequest, String userName, String token) throws ThirdPartyIntegrationException, URISyntaxException {
        // get user profile
        System.out.println("paymentRequest.getUsername()" + paymentRequest);
        System.out.println("paymentRequest.getUsername()" + paymentRequest.getUsername());
        UserProfileResponse userProfileResponse = operationService.getUserProfile(userName,token);

        log.info("HERE is the response from User Profile  ::: " + userProfileResponse.getReferenceCode());
        log.info("HERE is the response from User Profile isSmsAle ::: " + userProfileResponse.isSmsAlertConfig());
        //secure Payment
        String transactionId = CommonUtils.generatePaymentTransactionId();

        ThirdPartyNames thirdPartyName = categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        BigDecimal fee = billerConsumerFeeService.getFee(paymentRequest.getAmount(), thirdPartyName, paymentRequest.getBillerId());
        FeeBearer feeBearer = billerConsumerFeeService.getFeeBearer(thirdPartyName, paymentRequest.getBillerId());
        if (operationService.secureFund(paymentRequest.getAmount(), fee, userName, paymentRequest.getSourceWalletAccountNumber(), transactionId, feeBearer, token)) {
            try {
                PaymentResponse paymentResponse = getBillsPaymentService(paymentRequest.getCategoryId()).processMultiplePayment(paymentRequest, fee, transactionId, userName);
                //store the transaction information
                PaymentTransactionDetail paymentTransactionDetail = operationService.saveTransactionDetailMultiple(userProfileResponse, paymentRequest, fee, paymentResponse, userName, transactionId);
                // notify customer
                pushINAPP(paymentTransactionDetail, token, paymentResponse);
                pushEMAIL(paymentTransactionDetail, token, paymentResponse, userProfileResponse);

                // check if SMS is enabled
//                if (userProfileResponse.isSmsAlertConfig()){
//                    log.info("USER ENABLED SMS ALERT :::::: " + userProfileResponse.isSmsAlertConfig());
//                    SMSChargeResponse smsChargeResponse = operationService.getSMSCharges(token); // debit the customer for SMS
//                    log.info("smsChargeResponse ::::: {} :: " + smsChargeResponse);
//                    if (smsChargeResponse != null){
//                        log.info(" lets continue ::::: {} :: 2" + smsChargeResponse);
//                        sendSMSOperationMultiple(userProfileResponse,paymentTransactionDetail, paymentRequest, fee, userName, paymentRequest, paymentResponse, token, smsChargeResponse);
//                    }
//                }
                log.info("This is the status of userAlert config {} :::" + userProfileResponse.isSmsAlertConfig());

                /**
                 * check user type
                 * get commission for merchant user
                 * credit the merchant user's commission wallet
                 */


                //logTransaction(paymentRequest,paymentResponse,token,userName);
                Map<String, String> map = new HashMap<>();
                map.put("message", "Making Bulk Bills Payment");
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
                log.error("This is the error from payment :::: " + e.getMessage());
                //   operationService.saveFailedTransactionDetail(paymentRequest, fee, null, userName, null);
                //   disputeService.logTransactionAsDispute(userName, paymentRequest, thirdPartyName, paymentRequest.getBillerId(), paymentRequest.getCategoryId(), paymentRequest.getAmount(), fee, transactionId);

                throw new ThirdPartyIntegrationException(e.getHttpStatus(), e.getMessage());
            }
        }

        log.error("Unable to secure fund from user's wallet");
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }


    public ResponseEntity<?> processBulkPayment(MultipartFile file, HttpServletRequest request, String token) throws ThirdPartyIntegrationException, URISyntaxException, IOException {
        log.info("file inside::: " + file.getInputStream());
        PaymentResponse paymentResponse = null;
        if (ExcelHelper.hasExcelFormat(file)) {
            paymentResponse = buildBulkPayment(ExcelHelper.excelToPaymentRequest(file.getInputStream(),
                    file.getOriginalFilename()), request, token);
            log.info("file back from extraction ::: " + paymentResponse);
        }

        // build payment request
        return new ResponseEntity<>(new SuccessResponse(paymentResponse), HttpStatus.OK);
    }

    PaymentResponse buildBulkPayment(BulkBillsPaymentDTO bulkBillsPaymentDTO,HttpServletRequest request, String token) throws ThirdPartyIntegrationException, URISyntaxException {

        log.info("Just entered Here we are " + bulkBillsPaymentDTO);

        PaymentResponse paymentResponse = new PaymentResponse();
        MultiplePaymentRequest paymentRequest = new MultiplePaymentRequest();
        Map<String,String> map = new LinkedHashMap<>();

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

            paymentRequest.setData(data);

            log.info("Here we are " + bulkBillsPaymentDTO);

            processMultiplePayment(paymentRequest, mPayUser.getUserId(), token);

        }

        return paymentResponse;
    }

    public ResponseEntity<?> processBulkPaymentForm(List<MultiplePaymentRequest>  multipleFormPaymentRequest, String username, String token) throws ThirdPartyIntegrationException, URISyntaxException {

        System.out.println("multipleFormPaymentRequest ::: {} " + multipleFormPaymentRequest);
        List<MultiplePaymentRequest> paymentRequestList = multipleFormPaymentRequest;
        PaymentResponse paymentResponse = null;
        MultiplePaymentRequest paymentRequest = new MultiplePaymentRequest();
        for (int i = 0; i < paymentRequestList.size(); i++) {
            paymentRequest = paymentRequestList.get(i);
            System.out.println(" paymentRequest ::: {} " +paymentRequest );
            paymentResponse = processMultiplePayment(paymentRequest, username, token);
//            MultiplePaymentRequest finalPaymentRequest = paymentRequest;
//            System.out.println("finalPaymentRequest :: {} " +finalPaymentRequest);
//            CompletableFuture.runAsync(() -> {
//                try {
//                    processPayment(finalPaymentRequest, Constants.USERNAME, Constants.TOKEN);
//                } catch (ThirdPartyIntegrationException e) {
//                    e.printStackTrace();
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                }
//            });
            System.out.println("outter ::: " + paymentResponse);
        }


        // build payment request
        return new ResponseEntity<>(new SuccessResponse(paymentResponse), HttpStatus.OK);
    }



    public void pushINAPP(PaymentTransactionDetail paymentTransactionDetail, String token, PaymentResponse paymentResponse) throws ThirdPartyIntegrationException {
        InAppEvent inAppEvent = buildInAppNotificationObject(paymentTransactionDetail, token, EventType.IN_APP, paymentResponse);
        try {
           operationService.sendInAppNotification(inAppEvent, token);
        }catch (ThirdPartyIntegrationException ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }

    private void pushEMAIL(PaymentTransactionDetail paymentTransactionDetail, String token, PaymentResponse paymentResponse,UserProfileResponse userProfileResponse) throws ThirdPartyIntegrationException {

        EmailEvent emailEvent = new EmailEvent();
        emailEvent.setEventType(EventType.EMAIL.name());
        EmailPayload data = new EmailPayload();

        data.setMessage(getMessageDetails(paymentResponse, paymentTransactionDetail));

        EmailRecipient emailRecipient = new EmailRecipient();
        emailRecipient.setFullName(userProfileResponse.getSurname()+ " " +userProfileResponse.getFirstName() + " " +userProfileResponse.getMiddleName());
        emailRecipient.setEmail(userProfileResponse.getEmail());

        List<EmailRecipient> addUserId = new ArrayList<>();
        addUserId.add(emailRecipient);
        data.setNames(addUserId);

        emailEvent.setData(data);
        emailEvent.setInitiator(paymentTransactionDetail.getUsername());

        try {
            operationService.sendEmailNotification(emailEvent, token);
        }catch (ThirdPartyIntegrationException ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }

    private InAppEvent buildInAppNotificationObject(PaymentTransactionDetail paymentTransactionDetail, String token, EventType eventType, PaymentResponse paymentResponse){
        InAppEvent inAppEvent = new InAppEvent();
        inAppEvent.setEventType(eventType.name());

        InAppPayload data = new InAppPayload();
        data.setMessage(getMessageDetails(paymentResponse, paymentTransactionDetail));
        data.setType(eventType.name());

        InAppRecipient inAppRecipient = new InAppRecipient();
        inAppRecipient.setUserId(paymentTransactionDetail.getUsername());

        List<InAppRecipient> addUserId = new ArrayList<>();
        addUserId.add(inAppRecipient);

        data.setUsers(addUserId);

        inAppEvent.setData(data);
        inAppEvent.setInitiator(paymentTransactionDetail.getUsername());

        return inAppEvent;
    }

    private String getMessageDetails(PaymentResponse paymentResponse, PaymentTransactionDetail paymentTransactionDetail){
        PaymentResponse data = new PaymentResponse();
        List<ParamNameValue> valueList = paymentResponse.getData();
        String message = null;
        for (int i = 0; i < valueList.size(); i++) {
            ParamNameValue value = new ParamNameValue();
            value.setName(valueList.get(i).getName());
            value.setValue(valueList.get(i).getValue());
            message = "Your account has "+ "\n" +
                    ""+"been credited with:" + paymentTransactionDetail.getAmount() +" \n" +
                    "" + value.getValue();
//            message = "name :" + value.getName() +"  \"<br>\"" +
//            " \n" +  "Value : " + value.getValue();
        }
        return message;

    }

    @Async
    public void sendSMSOperation(UserProfileResponse userProfileResponse, PaymentTransactionDetail paymentTransactionDetail, PaymentRequest paymentRequest, BigDecimal fee, String userName, PaymentRequest paymentRequest2, PaymentResponse paymentResponse, String token, SMSChargeResponse smsChargeResponse) throws ThirdPartyIntegrationException, ExecutionException, InterruptedException {
        // get the SMS charge

        String transactionId = CommonUtils.generatePaymentTransactionId();


        if(operationService.secureFund(smsChargeResponse.getFee(), BigDecimal.valueOf(0.0), userName, paymentRequest2.getSourceWalletAccountNumber(), transactionId, null, token)){
            // Send SMS and save the transaction
            log.info(" After deducting money for sms  send notificaiton::::: {} Line 246" + smsChargeResponse);

            pushSMS(paymentTransactionDetail, token, paymentResponse, userProfileResponse);

            operationService.saveTransactionDetail(userProfileResponse, paymentRequest, BigDecimal.valueOf(0.0), null, userName, transactionId);

            paymentTransactionDetail.setAmount(smsChargeResponse.getFee());
            pushSMS(paymentTransactionDetail,token,paymentResponse, userProfileResponse); // send SMS for SMS charg debit
        }

    }

    @Async
    public void sendSMSOperationMultiple(UserProfileResponse userProfileResponse, PaymentTransactionDetail paymentTransactionDetail, MultiplePaymentRequest paymentRequest, BigDecimal fee, String userName, MultiplePaymentRequest paymentRequest2, PaymentResponse paymentResponse, String token, SMSChargeResponse smsChargeResponse) throws ThirdPartyIntegrationException, ExecutionException, InterruptedException {
        // get the SMS charge

        String transactionId = CommonUtils.generatePaymentTransactionId();

        if(operationService.secureFund(smsChargeResponse.getFee(), BigDecimal.valueOf(0.0), userName, paymentRequest2.getSourceWalletAccountNumber(), transactionId, null, token)){
            // Send SMS and save the transaction
            log.info(" After deducting money for sms  send notificaiton::::: {} Line 246" + smsChargeResponse);

            pushSMS(paymentTransactionDetail, token, paymentResponse, userProfileResponse);

            operationService.saveTransactionDetailMultiple(userProfileResponse,paymentRequest, BigDecimal.valueOf(0.0), null, userName, transactionId);

            paymentTransactionDetail.setAmount(smsChargeResponse.getFee());
            pushSMS(paymentTransactionDetail,token,paymentResponse, userProfileResponse); // send SMS for SMS charg debit
        }

    }


    public void pushSMS(PaymentTransactionDetail paymentTransactionDetail, String token, PaymentResponse paymentResponse, UserProfileResponse userProfileResponse) throws ThirdPartyIntegrationException {

        SmsEvent smsEvent = new SmsEvent();
        SmsPayload data = new SmsPayload();
        data.setMessage(getMessageDetails(paymentResponse, paymentTransactionDetail));

        SmsRecipient smsRecipient = new SmsRecipient();
        smsRecipient.setEmail(userProfileResponse.getEmail());
        smsRecipient.setTelephone(userProfileResponse.getPhoneNumber());
        List<SmsRecipient> addList = new ArrayList<>();
        addList.add(smsRecipient);

        data.setRecipients(addList);
        smsEvent.setData(data);

        smsEvent.setEventType(EventType.SMS.name());
        smsEvent.setInitiator(paymentTransactionDetail.getUsername());

        try {
            // check if User enables SMS charge
            notificationService.smsNotification(smsEvent,token);

        }catch (ThirdPartyIntegrationException ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }


    public Map<String, Object> search(String username, int pageNumber, int pageSize){
        Pageable paging = getPageable(pageNumber, pageSize);
        Page<TransactionDetail> transactionDetailPage = null;
        List<TransactionDetail> transactionDetailList = new ArrayList<>();

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
        Page<TransactionDetail> transactionDetailPage = null;
        List<TransactionDetail> transactionDetailList = new ArrayList<>();

        if (CommonUtils.isEmpty(referralCode)){
            transactionDetailPage = paymentTransactionRepo.getAllTransaction(paging);
            transactionDetailList = transactionDetailPage.getContent();
            return getTransactionMap(transactionDetailList,transactionDetailPage);
        }

        transactionDetailPage = paymentTransactionRepo.getAllTransactionByReferralCode(referralCode, paging);

        transactionDetailList = transactionDetailPage.getContent();

        List<TransactionDetail> transactionDetailPage2 = paymentTransactionRepo.getAllTransactionByReferralCodeGroupedBy(referralCode);

        List<TransactionDetail> transactionDetailList1 = new ArrayList<>();

        log.info("Transaction Size ::: {} " + transactionDetailPage2.size());
        log.info("Transaction List" + transactionDetailPage2);
        int count = 0;
       for (int i = 0; i < transactionDetailPage2.size(); i++) {
           count ++;
        }

        Map<String, Map<Object, Object>> map = new HashMap<>();
        Map<String, Object> map1 = new LinkedHashMap<>();
        Map<Object, Object> objectMap = null;
        for (int i = 0; i < transactionDetailPage2.size()-1; i++)
        {

            for (int j = i+1; j < transactionDetailPage2.size(); j++)
            {
//                log.info("Second ::: {} ");
//                System.out.println("i  " + transactionDetailPage2.get(i).getUsername());

                //&& (transactionDetailPage2.get(i) != transactionDetailPage2.get(j))
                if (transactionDetailPage2.get(i).getUsername().equalsIgnoreCase(transactionDetailPage2.get(j).getUsername()) && (i != j))
                {
                    log.info( j + "  ===inside :: {}===  " + i);

//                    objectMap = new HashMap<>();
//                    objectMap.put(transactionDetailPage2.get(i).getUsername(), "count");
//                    map.put("ID", objectMap);
//                    map1.putAll(map);
//                    TransactionDetail transactionDetail = transactionDetailPage2.get(i);
//                    transactionDetailList1.add(transactionDetail);
//                    log.info("Third ::: {} ");
                    System.out.println("Duplicate Element transactionDetailPage2.get(i) : "+transactionDetailPage2.get(j));
                }
            }
        }

//        log.info("Final :::" + transactionDetailList1.size());
//        log.info("MAP" + map1);
//
//        int[] my_array = {1, 2, 5, 5, 6, 6, 7, 2};
//
//        for (int i = 0; i < my_array.length-1; i++)
//        {
//            for (int j = i+1; j < my_array.length; j++)
//            {
//                System.out.println("j " + j);
//
//                if ((my_array[i] == my_array[j]) && (i != j))
//                {
//                    System.out.println( my_array[i] +"Duplicate Element : "+my_array[j]);
//                }
//            }
//        }

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

    public TransactionDetail searchTransactionByTransactionID(String transactionId) throws ThirdPartyIntegrationException {
        try{
            return paymentTransactionRepo.getAllTransactionByTransactionId(transactionId);
        }catch (Exception ex){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }

    //    //check for the userType whos Item has been purchased
//    // take percentage from payment and send as commission to the Agent
//    // get commission from commission user table
//
//    // as a merchant user i should be able to receive certain % amount commission anytime i use my waya app to make bilspayment
    public void getCommissionForMakingBillsPayment(String userId, String token, BigDecimal amount) throws ThirdPartyIntegrationException {
        log.info("Inside getCommissionForMakingBillsPayment::: " +userId );
        UserType userType = getUserType(userId,token);
        log.info("Billspyament:: {} ::: " + userType);

        if (userType !=null){
            MerchantCommissionTrackerDto trackerDto= new MerchantCommissionTrackerDto();
            trackerDto.setUserId(userId);
            trackerDto.setUserType(userType);
            trackerDto.setCommissionType(CommissionType.PERCENTAGE);
            trackerDto.setCommissionValue(BigDecimal.ONE.doubleValue());
            trackerDto.setTransactionType(TransactionType.BILLS_PAYMENT);
            commissionOperationService.saveMerchantCommission(trackerDto,token);

            payCommissionToMerchant(userType,userId,token, amount);  // pay user commission

        }

    }

    private UserProfileResponsePojo getUserProfile(String userId, String token) throws ThirdPartyIntegrationException {
        try {
            ResponseEntity<ApiResponseBody<UserProfileResponsePojo>> userProfile = authFeignClient.getUserByUserId(userId, token);
            ApiResponseBody<UserProfileResponsePojo> responseBody = userProfile.getBody();
            return responseBody.getData();
        }catch (Exception e){
            throw new ThirdPartyIntegrationException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }
        private UserType getUserType(String userId, String token) throws ThirdPartyIntegrationException {
            UserProfileResponsePojo userProfile =  getUserProfile(userId, token);
            log.info("userProfile :: " +userProfile.getRoles());
                for(String role : userProfile.getRoles()) {
                    if(UserType.ROLE_CORP.name().equalsIgnoreCase(role) || UserType.ROLE_CORP_ADMIN.name().equalsIgnoreCase(role)){
                        return UserType.ROLE_CORP ==null ? UserType.ROLE_CORP_ADMIN : UserType.ROLE_CORP;
                    }
                }
        return null;

    }


    public void payCommissionToMerchant(UserType userType,String userName, String token, BigDecimal amount) throws ThirdPartyIntegrationException {
        commissionOperationService.payUserCommission(userType,userName,token, amount); // log commission
    }

    //as a merchant user anytime i sell billspayment a certain % amount of the item sold amount is transferred on real time to my commission wallet from WAYA
    private void calculateMerchantPercentage(ThirdParty thirdParty, UserType userType,String userName, String token, BigDecimal amount) throws ThirdPartyIntegrationException {
        commissionOperationService.payUserCommission(userType,userName,token, amount); // log commission

        /**
         * 1. get the biller
         *
         */
    }

    /**
     *
     * @return
     */
    //check for the userType who's Item has been purchased
    public ThirdParty checkCustomerWhosItemIsBeanPurchased(){
        ThirdParty thirdParty = new ThirdParty();
        return thirdParty;
    }

    //ABILITY for waya admin with the right access and permission to select which of the waya official account to make the billspayment from
    public List<NewWalletResponse> adminSelectWayaOfficialAccount(String token) throws ThirdPartyIntegrationException {
        List<NewWalletResponse> newWalletResponses = operationService.getWayaOfficialWallet(token);
        return newWalletResponses;
    }

    //As a an agent aggregator user, I should receive a certain % amount of commission from WAYA when the agent that signed up with my referral code has received a commision from WAYA for the sell of billspayment



}