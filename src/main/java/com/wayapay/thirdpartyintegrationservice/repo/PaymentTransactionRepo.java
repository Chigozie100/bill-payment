package com.wayapay.thirdpartyintegrationservice.repo;

import com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepo extends JpaRepository<PaymentTransactionDetail, Long> {

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.paymentRequest, p.paymentResponse, p.createdAt) from PaymentTransactionDetail p where p.username = ?1 order by p.createdAt desc ")
    Page<TransactionDetail> getAllTransactionByUsername(String username, Pageable pageable);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.paymentRequest, p.paymentResponse, p.createdAt) from PaymentTransactionDetail p order by p.createdAt desc ")
    Page<TransactionDetail> getAllTransaction(Pageable pageable);
}
