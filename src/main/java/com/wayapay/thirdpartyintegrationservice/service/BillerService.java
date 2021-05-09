package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementRequest;
import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementResponse;
import com.wayapay.thirdpartyintegrationservice.dto.BillerResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.Biller;
import com.wayapay.thirdpartyintegrationservice.repo.BillerRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class BillerService {

    private final BillerRepo billerRepo;
    private final CategoryService categoryService;

    public List<BillerResponse> getAllActiveBillers(String categoryId) throws ThirdPartyIntegrationException {
        try {
            if (Objects.isNull(categoryId)){
                log.error("No categoryId provided");
                throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "No categoryId provided");
            }
            return billerRepo.findAllActiveBiller(categoryId);
        } catch (Exception exception) {
            log.error("unable to get All active billers", exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, ERROR_MESSAGE);
        }
    }

    //createBiller
    public ResponseHelper createBiller(BillerManagementRequest request) throws ThirdPartyIntegrationException {

        //confirm that the aggregatorId is valid
        isCategoryIdValid(request.getCategoryId());

        //confirm that name is unique
        doesNameExist(request.getName(), request.getCategoryId(), request.getId(), false);

        //confirm that aggregatorCode is unique
        doesAggregatorCodeExist(request.getBillerAggregatorCode(), request.getCategoryId(), request.getId(), false);

        Biller biller = generateBiller(new Biller(), request);
        biller = billerRepo.save(biller);

        return new SuccessResponse(new BillerManagementResponse(biller.getId(), biller.getName(), biller.getBillerAggregatorCode(), biller.getBillerWayaPayCode(), biller.isActive(), biller.getCategory().getId(), biller.getCategory().getName(), categoryService.findThirdPartyByCategoryAggregatorCode(biller.getCategory().getCategoryAggregatorCode()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, ERROR_MESSAGE))));
    }

    //UpdateBiller
    public ResponseHelper updateBiller(BillerManagementRequest request) throws ThirdPartyIntegrationException {

        if (Objects.isNull(request.getId())){
            log.error("No Id provided");
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.ID_IS_REQUIRED);
        }

        //confirm that the aggregatorId is valid
        isCategoryIdValid(request.getCategoryId());

        //getBiller
        Biller biller = findById(request.getId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_UNKNOWN));

        //confirm that name is unique
        doesNameExist(request.getName(), request.getCategoryId(), request.getId(), true);

        //confirm that aggregatorCode is unique
        doesAggregatorCodeExist(request.getBillerAggregatorCode(), request.getCategoryId(), request.getId(), true);

        biller = billerRepo.save(generateBiller(biller, request));
        return new SuccessResponse(new BillerManagementResponse(biller.getId(), biller.getName(), biller.getBillerAggregatorCode(), biller.getBillerWayaPayCode(), biller.isActive(), biller.getCategory().getId(), biller.getCategory().getName(), categoryService.findThirdPartyByCategoryAggregatorCode(biller.getCategory().getCategoryAggregatorCode()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, ERROR_MESSAGE))));
    }

    //getOne
    public SuccessResponse get(Long id) throws ThirdPartyIntegrationException {
        if (Objects.isNull(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_REQUIRED);
        }
        return new SuccessResponse(billerRepo.findBillerById(id).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_UNKNOWN)));
    }

    //getAll
    public SuccessResponse getAll() throws ThirdPartyIntegrationException {
        try {
            return new SuccessResponse(billerRepo.findAllBiller());
        } catch (Exception exception) {
            log.error("Unable to fetch all billers ",exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch all billers");
        }
    }

    //toggle
    public SuccessResponse toggle(Long id) throws ThirdPartyIntegrationException {
        if (Objects.isNull(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_REQUIRED);
        }

        Biller biller = billerRepo.findById(id).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_UNKNOWN));
        biller.setActive(!biller.isActive());
        biller = billerRepo.save(biller);
        return new SuccessResponse(biller.isActive() ? "successfully activated" : "successfully deactivated", new BillerManagementResponse(biller.getId(), biller.getName(), biller.getBillerAggregatorCode(), biller.getBillerWayaPayCode(), biller.isActive(), biller.getCategory().getId(), biller.getCategory().getName(), categoryService.findThirdPartyByCategoryAggregatorCode(biller.getCategory().getCategoryAggregatorCode()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, ERROR_MESSAGE))));
    }

    public List<Biller> findAllByCategoryId(Long categoryId) throws ThirdPartyIntegrationException {
        try {
            return billerRepo.findAllByCategoryId(categoryId);
        } catch (Exception exception) {
            log.error("Unable to fetch all biller by categoryId => {}", categoryId, exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, ERROR_MESSAGE);
        }
    }

    public void deleteAll(List<Biller> billerListNotInAPI){
        if (!billerListNotInAPI.isEmpty()) {
            billerRepo.deleteInBatch(billerListNotInAPI);
        }
    }

    public void saveAll(List<Biller> newBillerListToBeSaved){
        if(!newBillerListToBeSaved.isEmpty()){
            billerRepo.saveAll(newBillerListToBeSaved);
        }
    }

    //is categoryId valid(create/update)
    private void isCategoryIdValid(Long id) throws ThirdPartyIntegrationException {
        if (!categoryService.isCategoryIdValid(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid category Id provided");
        }
    }

    //does name already exist
    private void doesNameExist(String billerName, Long categoryId, Long billerId, boolean isUpdate) throws ThirdPartyIntegrationException {

        boolean status;
        if (isUpdate) {
            status = billerRepo.findByNameAndCategoryIdNotId(billerName, categoryId, billerId) > 0;
        } else {
            status = billerRepo.findByNameAndCategoryId(billerName, categoryId) > 0;
        }

        if (status){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Category name already exist");
        }
    }

    //does categoryAggregatorCode already exist
    private void doesAggregatorCodeExist(String categoryAggregatorCode, Long aggregatorId, Long categoryId, boolean isUpdate) throws ThirdPartyIntegrationException {

        boolean status;
        if (isUpdate) {
            status = billerRepo.findByBillerAggregatorCodeAndCategoryIdNotId(categoryAggregatorCode, aggregatorId, categoryId) > 0;
        } else {
            status = billerRepo.findByBillerAggregatorCodeAndCategoryId(categoryAggregatorCode, aggregatorId) > 0;
        }

        if (status){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Biller aggregator code already exist");
        }
    }

    private Biller generateBiller(Biller biller, BillerManagementRequest billerManagementRequest) throws ThirdPartyIntegrationException {
        biller.setBillerAggregatorCode(billerManagementRequest.getBillerAggregatorCode());
        biller.setBillerWayaPayCode(billerManagementRequest.getBillerWayaPayCode());
        biller.setName(billerManagementRequest.getName());
        biller.setCategory(categoryService.findById(billerManagementRequest.getCategoryId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch category")));
        return biller;
    }

    public Optional<Biller> findById(Long categoryId) {
        try{
            return billerRepo.findById(categoryId);
        } catch (Exception exception) {
            log.error("Unable to fetch biller by Id");
            return Optional.empty();
        }
    }

}
