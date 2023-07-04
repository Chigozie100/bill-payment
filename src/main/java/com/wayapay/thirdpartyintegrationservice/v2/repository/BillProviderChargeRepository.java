package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.entity.BillProviderCharge;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProvider;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillProviderChargeRepository extends JpaRepository<BillProviderCharge, Long> {
    Optional<BillProviderCharge> findByServiceProviderCategoryAndServiceProviderAndIsActiveAndIsDeleted(ServiceProviderCategory category, ServiceProvider provider, boolean isActive, boolean isDeleted);

    Optional<BillProviderCharge> findByServiceProviderAndServiceProviderCategoryAndIsActiveAndIsDeleted(ServiceProvider serviceProvider, ServiceProviderCategory category, boolean isActive, boolean isDeleted);

    Optional<BillProviderCharge> findByIdAndIsActiveAndIsDeleted(Long id, boolean isActive, boolean isDeleted);
}
