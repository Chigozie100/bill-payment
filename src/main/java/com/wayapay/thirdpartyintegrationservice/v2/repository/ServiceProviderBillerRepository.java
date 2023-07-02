package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderBiller;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderBillerRepository extends JpaRepository<ServiceProviderBiller, Long> {
    Optional<ServiceProviderBiller> findByNameAndServiceProviderCategoryAndIsActiveAndIsDeleted(String name, ServiceProviderCategory serviceProviderCategory, boolean isActive, boolean isDeleted);
    Optional<ServiceProviderBiller> findByIdAndIsActiveAndIsDeleted(Long id, boolean isActive, boolean isDeleted);
    Page<ServiceProviderBiller> findAllByServiceProviderCategoryAndIsActiveAndIsDeleted(ServiceProviderCategory serviceProviderCategory, boolean isActive, boolean isDeleted, Pageable pageable);
    Page<ServiceProviderBiller> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);

    Optional<ServiceProviderBiller> findByServiceProviderCategoryAndIsActiveAndIsDeletedAndNameIgnoreCaseLike(ServiceProviderCategory category, boolean isActive, boolean isDeleted, String name);

    List<ServiceProviderBiller> findAllByServiceProviderIdAndServiceProviderCategoryAndIsActiveAndIsDeleted(Long serviceProviderId, ServiceProviderCategory serviceProviderCategory, boolean isActive, boolean isDeleted);

    List<ServiceProviderBiller> findAllByServiceProviderCategoryAndIsActiveAndIsDeleted(ServiceProviderCategory serviceProviderCategory, boolean isActive, boolean isDeleted);

    Optional<ServiceProviderBiller> findByTypeAndServiceProviderCategoryAndIsActiveAndIsDeleted(String serviceType, ServiceProviderCategory serviceProviderCategory, boolean isActive, boolean isDeleted);

    Optional<ServiceProviderBiller> findByPrepaidNameOrPostpaidNameAndServiceProviderCategoryAndIsActiveAndIsDeleted(String prepaidService, String postpaidService, ServiceProviderCategory serviceProviderCategory,boolean isActive, boolean isDeleted);

    Optional<ServiceProviderBiller> findByIdAndServiceProviderIdAndIsActiveAndIsDeleted(Long id, Long serviceProviderId, boolean isActive, boolean isDeleted);
}
