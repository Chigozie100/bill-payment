package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.entity.BillProviderCharge;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProvider;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillProviderChargeRepository extends JpaRepository<BillProviderCharge, Long> {
    Optional<BillProviderCharge> findByServiceProviderCategoryAndServiceProviderAndIsActiveAndIsDeleted(ServiceProviderCategory category, ServiceProvider provider, boolean isActive, boolean isDeleted);

    Optional<BillProviderCharge> findByServiceProviderAndServiceProviderCategoryAndIsActiveAndIsDeleted(ServiceProvider serviceProvider, ServiceProviderCategory category, boolean isActive, boolean isDeleted);

    Optional<BillProviderCharge> findByIdAndIsActiveAndIsDeleted(Long id, boolean isActive, boolean isDeleted);

    Page<BillProviderCharge> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);

    Page<BillProviderCharge> findAllByServiceProviderAndIsActiveAndIsDeleted(ServiceProvider serviceProvider, boolean isActive, boolean isDeleted, Pageable pageable);
}
