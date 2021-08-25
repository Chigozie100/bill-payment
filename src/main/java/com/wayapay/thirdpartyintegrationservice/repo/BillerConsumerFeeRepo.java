package com.wayapay.thirdpartyintegrationservice.repo;

import com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeResponse;
import com.wayapay.thirdpartyintegrationservice.model.BillerConsumerFee;
import com.wayapay.thirdpartyintegrationservice.util.FeeBearer;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface BillerConsumerFeeRepo extends JpaRepository<BillerConsumerFee, Long> {

    @Query("select count(b.id) from BillerConsumerFee b where b.thirdPartyName = ?1 and b.biller = ?2")
    long countByThirdPartyNameAndBiller(ThirdPartyNames thirdPartyNames, String biller);

    @Query("select count(b.id) from BillerConsumerFee b where b.thirdPartyName = ?1 and b.biller = ?2 and b.id <> ?3")
    long countByThirdPartyNameAndBillerNotId(ThirdPartyNames thirdPartyNames, String biller, Long id);

    @Query("select b from BillerConsumerFee b where b.thirdPartyName = ?1 and b.biller = ?2 and b.active = true ")
    Optional<BillerConsumerFee> findByThirdPartyNameAndBiller(ThirdPartyNames thirdPartyName, String biller);

    @Query("select b.feeBearer from BillerConsumerFee b where b.thirdPartyName = ?1 and b.biller = ?2 and b.active = true ")
    FeeBearer findFeeBearerByThirdPartyNameAndBiller(ThirdPartyNames thirdPartyName, String biller);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeResponse(b.id, b.thirdPartyName, b.biller, b.feeType, b.feeBearer, b.value, b.maxFixedValueWhenPercentage, b.active) from BillerConsumerFee b")
    List<BillerConsumerFeeResponse> findAllConfigurations();

    @Query("select b from BillerConsumerFee b where b.biller = ?1 ")
    BillerConsumerFee findByBiller(String billerName);
}
