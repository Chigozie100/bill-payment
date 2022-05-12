package com.wayapay.thirdpartyintegrationservice.repo;

import com.wayapay.thirdpartyintegrationservice.dto.ThirdPartyResponse;
import com.wayapay.thirdpartyintegrationservice.model.Biller;
import com.wayapay.thirdpartyintegrationservice.model.Category;
import com.wayapay.thirdpartyintegrationservice.model.ThirdParty;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ThirdPartyRepo extends JpaRepository<ThirdParty, Long> {

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.ThirdPartyResponse(t.id, t.thirdPartyNames, t.active) from ThirdParty t")
    List<ThirdPartyResponse> findAllThirdParty();

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.ThirdPartyResponse(t.id, t.thirdPartyNames, t.active) from ThirdParty t where t.id = ?1 ")
    Optional<ThirdPartyResponse> findThirdPartyById(Long id);

    @Query("select t from ThirdParty t where t.thirdPartyNames not in ?1")
    List<ThirdParty> findAllNotInList(List<ThirdPartyNames> thirdPartyName);

    @Query("select t.thirdPartyNames from ThirdParty t")
    List<ThirdPartyNames> findAllThirdPartyNames();

    @Query("select t from ThirdParty t where t.active =:status ")
    List<ThirdParty> findAllActiveAggregators(boolean status);


    @Query("select t from ThirdParty t ")
    List<ThirdParty> findAllActiveAggregators();

}
