package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderBiller;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderProductRepository extends JpaRepository<ServiceProviderProduct, Long> {
    Optional<ServiceProviderProduct> findByNameAndServiceProviderBillerAndIsActiveAndIsDeleted(String name, ServiceProviderBiller serviceProviderBiller, boolean isActive, boolean isDeleted);
    Optional<ServiceProviderProduct> findByNameAndProductCodeAndServiceProviderBillerAndIsActiveAndIsDeleted(String name,String productCode, ServiceProviderBiller serviceProviderBiller, boolean isActive, boolean isDeleted);

    Optional<ServiceProviderProduct> findByIdAndIsActiveAndIsDeleted(Long id, boolean isActive, boolean isDeleted);
    Page<ServiceProviderProduct> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);
    Page<ServiceProviderProduct> findAllByServiceProviderBillerAndIsActiveAndIsDeleted(ServiceProviderBiller serviceProviderBiller, boolean isActive, boolean isDeleted, Pageable pageable);
    List<ServiceProviderProduct> findAllByServiceProviderBillerAndIsActiveAndIsDeleted(ServiceProviderBiller serviceProviderBiller, boolean isActive, boolean isDeleted);

    Optional<ServiceProviderProduct> findByNameAndTypeAndServiceProviderBillerAndIsActiveAndIsDeleted(String name, String type, ServiceProviderBiller biller, boolean isActive, boolean isDeleted);

    Optional<ServiceProviderProduct> findFirstByProductCodeAndServiceProviderBillerAndIsActiveAndIsDeleted(String type, ServiceProviderBiller biller, boolean isActive, boolean isDeleted);

    Optional<ServiceProviderProduct> findFirstByTypeAndServiceProviderBillerAndIsActiveAndIsDeleted(String type, ServiceProviderBiller biller, boolean isActive, boolean isDeleted);

    Optional<ServiceProviderProduct> findByIdAndIsDeleted(Long id, boolean isDeleted);

    Optional<ServiceProviderProduct> findFirstByNameAndProductCodeAndServiceProviderBillerAndIsActiveAndIsDeleted(String name, String code, ServiceProviderBiller biller, boolean isActive, boolean isDeleted);
}
