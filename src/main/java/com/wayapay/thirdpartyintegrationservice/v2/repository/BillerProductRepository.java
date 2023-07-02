//package com.wayapay.thirdpartyintegrationservice.v2.repository;
//
//import com.wayapay.thirdpartyintegrationservice.v2.entity.BillerCategory;
//import com.wayapay.thirdpartyintegrationservice.v2.entity.BillerProduct;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface BillerProductRepository extends JpaRepository<BillerProduct,Long> {
//    List<BillerProduct> findAllByBillerCategoryAndIsActiveAndIsDeleted(BillerCategory billerCategory, boolean isActive,boolean isDeleted);
//    Page<BillerProduct> findAllByBillerCategoryAndIsActiveAndIsDeleted(BillerCategory billerCategory, boolean isActive,boolean isDeleted,Pageable pageable);
//    List<BillerProduct> findAllByNameAndBillerCategoryAndIsActiveAndIsDeleted(String name,BillerCategory billerCategory, boolean isActive,boolean isDeleted);
//    Page<BillerProduct> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);
//    Optional<BillerProduct> findByNameAndBillerCategoryAndIsActiveAndIsDeleted(String name,BillerCategory billerCategory, boolean isActive,boolean isDeleted);
//    Optional<BillerProduct> findByNameAndBillerCategoryAndHasBundlesAndIsActiveAndIsDeleted(String name,BillerCategory billerCategory,boolean hasBundle, boolean isActive,boolean isDeleted);
//    Optional<BillerProduct> findByNameAndBillerCategoryAndValidateCustomerIdAndIsActiveAndIsDeleted(String name,BillerCategory billerCategory,boolean validateCustomerId, boolean isActive,boolean isDeleted);
//    List<BillerProduct> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted);
//    Optional<BillerProduct> findByIdAndIsActiveAndIsDeleted(Long id, boolean isActive, boolean isDeleted);
//}
