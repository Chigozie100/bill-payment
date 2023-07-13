package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderProduct;
import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProviderProductBundle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderProductBundleRepository extends JpaRepository<ServiceProviderProductBundle,Long> {
    Optional<ServiceProviderProductBundle> findByNameAndServiceProviderProductAndIsActiveAndIsDeleted(String name, ServiceProviderProduct serviceProviderProduct, boolean isActive, boolean isDeleted);
    Optional<ServiceProviderProductBundle> findByNameAndBundleCodeAndAmountAndServiceProviderProductAndIsActiveAndIsDeleted(String name, String code, BigDecimal amount, ServiceProviderProduct serviceProviderProduct, boolean isActive, boolean isDeleted);
    Optional<ServiceProviderProductBundle> findByNameAndBundleCodeAndServiceProviderProductAndIsActiveAndIsDeleted(String name, String code, ServiceProviderProduct serviceProviderProduct, boolean isActive, boolean isDeleted);
    Optional<ServiceProviderProductBundle> findByNameAndBundleCodeAndMonthsPaidForAndServiceProviderProductAndIsActiveAndIsDeleted(String name, String code, String monthsPaidFor, ServiceProviderProduct serviceProviderProduct, boolean isActive, boolean isDeleted);
    Page<ServiceProviderProductBundle> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);
    List<ServiceProviderProductBundle> findAllByServiceProviderProductAndIsActiveAndIsDeleted(ServiceProviderProduct providerProduct,boolean isActive, boolean isDeleted);
    Page<ServiceProviderProductBundle> findAllByServiceProviderProductAndIsActiveAndIsDeleted(ServiceProviderProduct providerProduct,boolean isActive, boolean isDeleted,Pageable pageable);
    List<ServiceProviderProductBundle> findAllByServiceProviderProductAndBundleCodeAndIsActiveAndIsDeleted(ServiceProviderProduct providerProduct,String bundleCode,boolean isActive, boolean isDeleted);

    Optional<ServiceProviderProductBundle> findByIdAndIsActiveAndIsDeleted(Long id, boolean isActive, boolean isDeleted);

    Optional<ServiceProviderProductBundle> findByIdAndIsDeleted(Long id, boolean isDeleted);
}
