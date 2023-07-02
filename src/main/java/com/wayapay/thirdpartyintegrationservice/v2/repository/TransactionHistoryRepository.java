package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    Optional<TransactionHistory> findBySenderUserIdAndPaymentReferenceNumber(String userId, String referenceNumber);
    Optional<TransactionHistory> findBySenderUserIdAndPaymentReferenceNumberAndCategoryName(String userId, String referenceNumber, BillCategoryName categoryName);

    Optional<TransactionHistory> findByPaymentReferenceNumberAndSenderUserIdAndIsActiveAndIsDeleted(String reference, String id, boolean isActive, boolean isDeleted);
}
