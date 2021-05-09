package com.wayapay.thirdpartyintegrationservice.repo;

import com.wayapay.thirdpartyintegrationservice.dto.CategoryManagementResponse;
import com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse;
import com.wayapay.thirdpartyintegrationservice.model.Category;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {

    @Query("select count(c.id) from Category c where c.name = ?1 and c.thirdParty.id = ?2 ")
    long findByNameAndAggregatorId(String categoryName, Long aggregatorId);

    @Query("select count(c.id) from Category c where c.name = ?1 and c.thirdParty.id = ?2 and c.id <> ?3 ")
    long findByNameAndAggregatorIdNotId(String categoryName, Long aggregatorId, Long categoryId);

    @Query("select count(c.id) from Category c where c.categoryAggregatorCode = ?1 and c.thirdParty.id = ?2 ")
    long findByCategoryAggregatorCodeAndAggregatorId(String categoryAggregatorCode, Long aggregatorId);

    @Query("select count(c.id) from Category c where c.categoryAggregatorCode = ?1 and c.thirdParty.id = ?2 and c.id <> ?3 ")
    long findByCategoryAggregatorCodeAndAggregatorIdNotId(String categoryAggregatorCode, Long aggregatorId, Long categoryId);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.CategoryManagementResponse(c.id, c.name, c.categoryAggregatorCode, c.categoryWayaPayCode, c.active, c.thirdParty.id, c.thirdParty.thirdPartyNames) from Category c where c.id = ?1 ")
    Optional<CategoryManagementResponse> findCategoryById(Long id);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.CategoryManagementResponse(c.id, c.name, c.categoryAggregatorCode, c.categoryWayaPayCode, c.active, c.thirdParty.id, c.thirdParty.thirdPartyNames) from Category c ")
    List<CategoryManagementResponse> findAllCategory();

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.CategoryResponse(c.categoryAggregatorCode, c.name, c.categoryWayaPayCode) from Category c where c.active = true ")
    List<CategoryResponse> findAllActiveCategory();

    @Query("select c from Category c where c.thirdParty.id = ?1 ")
    List<Category> findAllByAggregator(Long aggregatorId);

    @Query("select c.thirdParty.thirdPartyNames from Category c where c.categoryAggregatorCode = ?1")
    ThirdPartyNames findThirdPartyNameByCategoryAggregatorCode(String categoryAggregatorCode);

}
