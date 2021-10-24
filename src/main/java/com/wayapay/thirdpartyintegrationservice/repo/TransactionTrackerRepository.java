package com.wayapay.thirdpartyintegrationservice.repo;

import com.wayapay.thirdpartyintegrationservice.model.TransactionTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionTrackerRepository extends JpaRepository<TransactionTracker, Long> {

    @Query("select c from TransactionTracker c where c.referreeId = :referreeId")
    Optional<TransactionTracker> findByReferreeId(@Param("referreeId") String referreeId);
}
