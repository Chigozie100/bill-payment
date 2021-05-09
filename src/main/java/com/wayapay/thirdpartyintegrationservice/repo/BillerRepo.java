package com.wayapay.thirdpartyintegrationservice.repo;

import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementResponse;
import com.wayapay.thirdpartyintegrationservice.dto.BillerResponse;
import com.wayapay.thirdpartyintegrationservice.model.Biller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BillerRepo extends JpaRepository<Biller, Long> {

    @Query("select count(b.id) from Biller b where b.name = ?1 and b.category.id = ?2 and b.id <> ?3 ")
    long findByNameAndCategoryIdNotId(String billerName, Long categoryId, Long billerId);

    @Query("select count(b.id) from Biller b where b.name = ?1 and b.category.id = ?2 ")
    long findByNameAndCategoryId(String billerName, Long categoryId);

    @Query("select count(b.id) from Biller b where b.billerAggregatorCode = ?1 and b.category.id = ?2 and b.id <> ?3")
    long findByBillerAggregatorCodeAndCategoryIdNotId(String categoryAggregatorCode, Long aggregatorId, Long categoryId);

    @Query("select count(b.id) from Biller b where b.billerAggregatorCode = ?1 and b.category.id = ?2 ")
    long findByBillerAggregatorCodeAndCategoryId(String categoryAggregatorCode, Long aggregatorId);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.BillerManagementResponse(b.id, b.name, b.billerAggregatorCode, b.billerWayaPayCode, b.active, b.category.id, b.category.name, t.thirdPartyNames) from Biller b join b.category.thirdParty t where b.id = ?1 ")
    Optional<BillerManagementResponse> findBillerById(Long id);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.BillerManagementResponse(b.id, b.name, b.billerAggregatorCode, b.billerWayaPayCode, b.active, b.category.id, b.category.name, t.thirdPartyNames) from Biller b join b.category.thirdParty t ")
    List<BillerManagementResponse> findAllBiller();

    @Query("select b from Biller b where b.category.id = ?1 ")
    List<Biller> findAllByCategoryId(Long categoryId);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.BillerResponse(b.billerAggregatorCode, b.name, b.billerWayaPayCode, b.category.categoryAggregatorCode) from Biller b where b.category.categoryAggregatorCode =?1 and b.active = true ")
    List<BillerResponse> findAllActiveBiller(String categoryId);
}
