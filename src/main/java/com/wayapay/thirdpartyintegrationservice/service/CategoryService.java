package com.wayapay.thirdpartyintegrationservice.service;

import com.wayapay.thirdpartyintegrationservice.dto.CategoryManagementRequest;
import com.wayapay.thirdpartyintegrationservice.dto.CategoryManagementResponse;
import com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse;
import com.wayapay.thirdpartyintegrationservice.exceptionhandling.ThirdPartyIntegrationException;
import com.wayapay.thirdpartyintegrationservice.model.Category;
import com.wayapay.thirdpartyintegrationservice.repo.CategoryRepo;
import com.wayapay.thirdpartyintegrationservice.responsehelper.ResponseHelper;
import com.wayapay.thirdpartyintegrationservice.responsehelper.SuccessResponse;
import com.wayapay.thirdpartyintegrationservice.util.Constants;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.wayapay.thirdpartyintegrationservice.util.Constants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final ThirdPartyService thirdPartyService;

    public List<CategoryResponse> getAllActiveCategories() throws ThirdPartyIntegrationException {
        try {
            return categoryRepo.findAllActiveCategory();
        } catch (Exception exception) {
            log.error("unable to get All active categories", exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, ERROR_MESSAGE);
        }
    }

    public Optional<ThirdPartyNames> findThirdPartyByCategoryAggregatorCode(String categoryAggregatorCode){
        try {
            return Optional.ofNullable(categoryRepo.findThirdPartyNameByCategoryAggregatorCode(categoryAggregatorCode));
        } catch (Exception exception) {
            log.error("Unable to fetch AggregatorName by categoryAggregatorCode ", exception);
            return Optional.empty();
        }
    }
    
     public ThirdPartyNames findThirdPartyByCategoryAggregatorName(String categoryAggregatorCode){
        try {
           ThirdPartyNames category = categoryRepo.findThirdPartyNameByCategoryAggregatorCode(categoryAggregatorCode);
           return category;
        } catch (Exception exception) {
            log.error("Unable to fetch AggregatorName by categoryAggregatorCode ", exception);
            return null;
        }
    }

    //createCategory
    public ResponseHelper createCategory(CategoryManagementRequest request) throws ThirdPartyIntegrationException {

        //confirm that the aggregatorId is valid
        isAggregatorValid(request.getAggregatorId());

        //confirm that name is unique
        doesNameExist(request.getName(), request.getAggregatorId(), request.getId(), false);

        //confirm that aggregatorCode is unique
        doesAggregatorCodeExist(request.getCategoryAggregatorCode(), request.getAggregatorId(), request.getId(), false);

        Category category = generateCategory(new Category(), request);
        category = categoryRepo.save(category);
        return new SuccessResponse(new CategoryManagementResponse(category.getId(), category.getName(), category.getCategoryAggregatorCode(), category.getCategoryWayaPayCode(), category.isActive(), category.getThirdParty().getId(), category.getThirdParty().getThirdPartyNames()));

    }

    //UpdateCategory
    public ResponseHelper updateCategory(CategoryManagementRequest request) throws ThirdPartyIntegrationException {

        if (!Objects.isNull(request.getId())){
            log.error("No Id provided");
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, Constants.ID_IS_REQUIRED);
        }

        //confirm that the aggregatorId is valid
        isAggregatorValid(request.getAggregatorId());

        //getCategory
        Category category = findById(request.getId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_UNKNOWN));

        //confirm that name is unique
        doesNameExist(request.getName(), request.getAggregatorId(), request.getId(), true);

        //confirm that aggregatorCode is unique
        doesAggregatorCodeExist(request.getCategoryAggregatorCode(), request.getAggregatorId(), request.getId(), true);

        category = categoryRepo.save(generateCategory(category, request));
        return new SuccessResponse(new CategoryManagementResponse(category.getId(), category.getName(), category.getCategoryAggregatorCode(), category.getCategoryWayaPayCode(), category.isActive(), category.getThirdParty().getId(), category.getThirdParty().getThirdPartyNames()));

    }

    //getOne
    public SuccessResponse get(Long id) throws ThirdPartyIntegrationException {
        if (Objects.isNull(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_REQUIRED);
        }
        return new SuccessResponse(categoryRepo.findCategoryById(id).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_UNKNOWN)));
    }

    //getAll
    public SuccessResponse getAll() throws ThirdPartyIntegrationException {
        try {
            return new SuccessResponse(categoryRepo.findAllCategory());
        } catch (Exception exception) {
            log.error("Unable to fetch all category ",exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch all category");
        }
    }

    //toggle
    public SuccessResponse toggle(Long id) throws ThirdPartyIntegrationException {
        if (Objects.isNull(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_REQUIRED);
        }

        Category category = categoryRepo.findById(id).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, ID_IS_UNKNOWN));
        category.setActive(!category.isActive());
        category = categoryRepo.save(category);
        return new SuccessResponse(category.isActive() ? "successfully activated" : "successfully deactivated", new CategoryManagementResponse(category.getId(), category.getName(), category.getCategoryAggregatorCode(), category.getCategoryWayaPayCode(), category.isActive(), category.getThirdParty().getId(), category.getThirdParty().getThirdPartyNames()));
    }

    //is aggregatorId valid(create/update)
    private void isAggregatorValid(Long id) throws ThirdPartyIntegrationException {
        if (!thirdPartyService.isAggregatorIdValid(id)){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Invalid aggregator Id provided");
        }
    }

    public List<Category> findAllByAggregator(Long thirdPartyId) throws ThirdPartyIntegrationException {
        try {
            return categoryRepo.findAllByAggregator(thirdPartyId);
        } catch (Exception exception) {
            log.error("Unable to fetch all categories by aggregatorId", exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, ERROR_MESSAGE);
        }
    }

    public void deleteAll(List<Category> categoryListNotInAPI){
        if (!categoryListNotInAPI.isEmpty()) {
            categoryRepo.deleteInBatch(categoryListNotInAPI);
        }
    }

    public void saveAll(List<Category> newCategoryListToBeSaved){
        if(!newCategoryListToBeSaved.isEmpty()){
            categoryRepo.saveAll(newCategoryListToBeSaved);
        }
    }

    //does name already exist
    private void doesNameExist(String categoryName, Long aggregatorId, Long categoryId, boolean isUpdate) throws ThirdPartyIntegrationException {

        boolean status;
        if (isUpdate) {
            status = categoryRepo.findByNameAndAggregatorIdNotId(categoryName, aggregatorId, categoryId) > 0;
        } else {
            status = categoryRepo.findByNameAndAggregatorId(categoryName, aggregatorId) > 0;
        }

        if (status){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Category name already exist");
        }
    }

    //does categoryAggregatorCode already exist
    private void doesAggregatorCodeExist(String categoryAggregatorCode, Long aggregatorId, Long categoryId, boolean isUpdate) throws ThirdPartyIntegrationException {

        boolean status;
        if (isUpdate) {
            status = categoryRepo.findByCategoryAggregatorCodeAndAggregatorIdNotId(categoryAggregatorCode, aggregatorId, categoryId) > 0;
        } else {
            status = categoryRepo.findByCategoryAggregatorCodeAndAggregatorId(categoryAggregatorCode, aggregatorId) > 0;
        }

        if (status){
            throw new ThirdPartyIntegrationException(HttpStatus.BAD_REQUEST, "Category aggregator code already exist");
        }
    }

    private Category generateCategory(Category category, CategoryManagementRequest categoryManagementRequest) throws ThirdPartyIntegrationException {
        category.setCategoryAggregatorCode(categoryManagementRequest.getCategoryAggregatorCode());
        category.setCategoryWayaPayCode(categoryManagementRequest.getCategoryWayaPayCode());
        category.setName(categoryManagementRequest.getName());
        category.setThirdParty(thirdPartyService.findById(categoryManagementRequest.getAggregatorId()).orElseThrow(() -> new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, "Unable to fetch aggregator")));
        return category;
    }

    public boolean isCategoryIdValid(Long id) throws ThirdPartyIntegrationException {
        try {
            return categoryRepo.existsById(id);
        } catch (Exception exception) {
            log.error("Unable to confirm if categoryId is valid or not valid", exception);
            throw new ThirdPartyIntegrationException(HttpStatus.EXPECTATION_FAILED, Constants.ERROR_MESSAGE);
        }
    }

    public Optional<Category> findById(Long categoryId) {
        try{
            return categoryRepo.findById(categoryId);
        } catch (Exception exception) {
            log.error("Unable to fetch category by Id");
            return Optional.empty();
        }
    }

    public List<Category> findAll(){
        try {
            return categoryRepo.findAll();
        } catch (Exception exception) {
            log.error("unable to fetch all category");
            return new ArrayList<>();
        }
    }
}
