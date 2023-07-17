package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.PaymentStatus;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderBiller;
import com.wayapay.thirdpartyintegrationservice.v2.entity.TransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    Optional<TransactionHistory> findBySenderUserIdAndPaymentReferenceNumber(String userId, String referenceNumber);
    Optional<TransactionHistory> findBySenderUserIdAndPaymentReferenceNumberAndCategoryName(String userId, String referenceNumber, BillCategoryName categoryName);

    Optional<TransactionHistory> findByPaymentReferenceNumberAndSenderUserIdAndIsActiveAndIsDeleted(String reference, String id, boolean isActive, boolean isDeleted);

    long countByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionHistory t where t.status=?1 AND t.amount IS NOT NULL")
    BigDecimal sumAmount(PaymentStatus status);

    List<TransactionHistory> findAllByServiceProviderBiller(ServiceProviderBiller serviceProviderBiller);
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionHistory t where t.status=?1 AND t.amount IS NOT NULL AND t.serviceProviderBiller.serviceProviderId=?2 ")
    BigDecimal sumAmountByProviders(PaymentStatus status, Long serviceProviderId);

    Page<TransactionHistory> findAllByServiceProviderBiller_ServiceProviderId(Long serviceProviderId, Pageable pageable);
    Page<TransactionHistory> findAllByCategoryName(BillCategoryName categoryName, Pageable pageable);
    Page<TransactionHistory> findAllByAccountNumber(String accountNumber, Pageable pageable);
    Page<TransactionHistory> findAllBySenderEmail(String email, Pageable pageable);
    Page<TransactionHistory> findAllByStatus(PaymentStatus status, Pageable pageable);
    Page<TransactionHistory> findAllByPaymentReferenceNumber(String reference, Pageable pageable);

    Page<TransactionHistory> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
