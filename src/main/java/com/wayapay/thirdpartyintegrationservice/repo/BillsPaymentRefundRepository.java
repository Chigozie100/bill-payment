package com.wayapay.thirdpartyintegrationservice.repo;

import com.wayapay.thirdpartyintegrationservice.model.BillsPaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillsPaymentRefundRepository extends JpaRepository<BillsPaymentRefund, Long> {
}
