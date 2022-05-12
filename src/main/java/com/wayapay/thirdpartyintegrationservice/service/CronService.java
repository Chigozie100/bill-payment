package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.config.ProfileDetailsService;
import com.wayapay.thirdpartyintegrationservice.dto.BillerResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.Biller;
import com.wayapay.thirdpartyintegrationservice.model.Category;
import com.wayapay.thirdpartyintegrationservice.service.auth.AuthFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.ERROR_MESSAGE;
import static com.wayapay.thirdpartyintegrationservice.util.Constants.SYNCED_SUCCESSFULLY;

@Slf4j
@RequiredArgsConstructor
@Service
public class CronService {


    private final CategoryService categoryService;
    private final BillerService billerService;
    private final ThirdPartyService thirdPartyService;
    private final AuthFeignClient authFeignClient;
    private final ProfileDetailsService profileDetailsService;
    private final BillsPaymentService billsPaymentService;

    private void syncBillers(){

    }

//    @Scheduled(cron="0 0 0 * * ?")
    @Scheduled(cron = "0 10 23 L * ?")
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
                    billerResponseListFromApi = billsPaymentService.getBillsPaymentService(category.getThirdParty().getThirdPartyNames()).getAllBillersByCategory(category.getCategoryAggregatorCode());

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

}
