package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProvider;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderCategoryRepository extends JpaRepository<ServiceProviderCategory, Long> {
    Optional<ServiceProviderCategory> findByNameAndServiceProviderAndIsActiveAndIsDeleted(String name, ServiceProvider serviceProvider, boolean isActive, boolean isDeleted);
    Optional<ServiceProviderCategory> findByIdAndIsActiveAndIsDeleted(Long id, boolean isActive, boolean isDeleted);
    Page<ServiceProviderCategory> findAllByServiceProviderAndIsActiveAndIsDeleted(ServiceProvider serviceProvider, boolean isActive, boolean isDeleted, Pageable pageable);
    Page<ServiceProviderCategory> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);
    List<ServiceProviderCategory> findAllByServiceProviderAndIsActiveAndIsDeleted(ServiceProvider serviceProvider, boolean isActive, boolean isDeleted);
    Optional<ServiceProviderCategory> findByServiceProviderAndTypeAndIsActiveAndIsDeleted(ServiceProvider serviceProvider,String type, boolean isActive, boolean isDeleted);
    Optional<ServiceProviderCategory> findByIdAndServiceProviderAndIsActiveAndIsDeleted(Long id, ServiceProvider serviceProvider, boolean isActive, boolean isDeleted);
    Optional<ServiceProviderCategory> findByTypeAndServiceProviderAndIsActiveAndIsDeleted(String type, ServiceProvider serviceProvider, boolean isActive, boolean isDeleted);
}
