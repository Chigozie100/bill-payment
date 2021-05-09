package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.*;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.Biller;
import com.wayapay.thirdpartyintegrationservice.model.Category;
import com.wayapay.thirdpartyintegrationservice.model.ThirdParty;
import com.wayapay.thirdpartyintegrationservice.repo.PaymentTransactionRepo;
import com.wayapay.thirdpartyintegrationservice.service.baxi.BaxiService;
import com.wayapay.thirdpartyintegrationservice.service.dispute.DisputeService;
import com.wayapay.thirdpartyintegrationservice.service.interswitch.QuickTellerService;
import com.wayapay.thirdpartyintegrationservice.service.itex.ItexService;
import com.wayapay.thirdpartyintegrationservice.util.CommonUtils;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.FeeBearer;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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

    private final CategoryService categoryService;
    private final BillerService billerService;
    private final ThirdPartyService thirdPartyService;

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

    public PaymentResponse processPayment(PaymentRequest paymentRequest, String userName) throws ThirdPartyIntegrationException {

        //secure Payment
        String transactionId = null;
        try {
            transactionId = CommonUtils.generatePaymentTransactionId();
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to generate transaction Id", e);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }

        ThirdPartyNames thirdPartyName = categoryService.findThirdPartyByCategoryAggregatorCode(paymentRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE));
        BigDecimal fee = billerConsumerFeeService.getFee(paymentRequest.getAmount(), thirdPartyName, paymentRequest.getBillerId());
        FeeBearer feeBearer = billerConsumerFeeService.getFeeBearer(thirdPartyName, paymentRequest.getBillerId());
        if (operationService.secureFund(paymentRequest.getAmount(), fee, userName, paymentRequest.getSourceWalletAccountNumber(), transactionId, feeBearer)){
            try {
                PaymentResponse paymentResponse = getBillsPaymentService(paymentRequest.getCategoryId()).processPayment(paymentRequest, fee, transactionId, userName);
                //store the transaction information
                operationService.saveTransactionDetail(paymentRequest, fee, paymentResponse, userName, transactionId);
                return paymentResponse;
            } catch (ThirdPartyIntegrationException e) {
                disputeService.logTransactionAsDispute(userName, paymentRequest, thirdPartyName, paymentRequest.getBillerId(), paymentRequest.getCategoryId(), paymentRequest.getAmount(), fee, transactionId);
                throw new ThirdPartyIntegrationException(e.getHttpStatus(), e.getMessage());
            }
        }

        log.error("Unable to secure fund from user's wallet");
        throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
    }

    public Page<TransactionDetail> search(String username, int pageNumber, int pageSize){
        if (CommonUtils.isEmpty(username)){
            return paymentTransactionRepo.getAllTransaction(PageRequest.of(pageNumber, pageSize));
        }
        return paymentTransactionRepo.getAllTransactionByUsername(username, PageRequest.of(pageNumber, pageSize));
    }
}
