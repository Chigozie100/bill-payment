package com.wayapay.thirdpartyintegrationservice.repo;

import com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepo extends JpaRepository<PaymentTransactionDetail, Long> {

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.referralCode, p.paymentRequest, p.paymentResponse, p.createdAt, p.username, p.email, p.userAccountNumber) from PaymentTransactionDetail p where p.username = ?1 order by p.createdAt desc ")
    Page<TransactionDetail> getAllTransactionByUsername(String username, Pageable pageable);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.referralCode, p.paymentRequest, p.paymentResponse,  p.createdAt, p.username, p.email, p.userAccountNumber) from PaymentTransactionDetail p where p.referralCode =:referralCode order by p.createdAt desc ")
    Page<TransactionDetail> getAllTransactionByReferralCode(String referralCode, Pageable pageable);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.referralCode, p.paymentRequest, p.paymentResponse,  p.createdAt, p.username, p.email, p.userAccountNumber) from PaymentTransactionDetail p where p.referralCode =:referralCode order by p.createdAt desc ")
    List<TransactionDetail> getAllTransactionByReferralCodeGroupedBy(String referralCode);

    @Query("select count(p.id) from PaymentTransactionDetail p where p.referralCode =:referralCode")
    long getAllTransactionByReferralCode(String referralCode);


    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.referralCode, p.paymentRequest, p.paymentResponse,  p.createdAt, p.username, p.email, p.userAccountNumber) from PaymentTransactionDetail p where  p.successful =:status order by p.createdAt desc ")
    Page<TransactionDetail> getAllTransactionBySuccessful(boolean status, Pageable pageable);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.referralCode, p.paymentRequest, p.paymentResponse,  p.createdAt, p.username, p.email, p.userAccountNumber) from PaymentTransactionDetail p order by p.createdAt desc ")
    Page<TransactionDetail> getAllTransactionByUserAccountNumber(String userAccountNumber, Pageable pageable);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.referralCode, p.paymentRequest, p.paymentResponse,  p.createdAt, p.username, p.email, p.userAccountNumber) from PaymentTransactionDetail p where p.transactionId =:transactionID order by p.createdAt desc ")
    TransactionDetail getAllTransactionByTransactionId(String transactionID);


    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.referralCode, p.paymentRequest, p.paymentResponse, p.createdAt, p.username, p.email, p.userAccountNumber) from PaymentTransactionDetail p order by p.createdAt desc ")
    Page<TransactionDetail> getAllTransaction(Pageable pageable);

    @Query("select count(p.id) from PaymentTransactionDetail p where p.username =:username")
    long findByUsername(String username);


    @Query("select p from PaymentTransactionDetail p where p.id = ?1 and p.isResolved=false")
     Optional<PaymentTransactionDetail> findByTransId(Long id);

    @Query("select new com.wayapay.thirdpartyintegrationservice.dto.TransactionDetail(p.transactionId, p.thirdPartyName, p.amount, p.successful, p.category, p.biller, p.referralCode, p.paymentRequest, p.paymentResponse, p.createdAt, p.username, p.email, p.userAccountNumber) from PaymentTransactionDetail p where p.transactionId = ?1")
    Optional<TransactionDetail> findByTransactionId(@Param("transactionId") String transactionId);

}
