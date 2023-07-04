//package com.wayapay.thirdpartyintegrationservice.v2.repository;
//
//import com.wayapay.thirdpartyintegrationservice.v2.entity.BillerProduct;
//import com.wayapay.thirdpartyintegrationservice.v2.entity.BillerProductBundle;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface BillerProductBundleRepository extends JpaRepository<BillerProductBundle, Long> {
//    Optional<BillerProductBundle> findByIdAndBillerProductAndIsActiveAndIsDeleted(Long id, BillerProduct billerProduct, boolean isActive,boolean isDeleted);
//    Optional<BillerProductBundle> findByNameAndBillerProductAndIsActiveAndIsDeleted(String name, BillerProduct billerProduct, boolean isActive,boolean isDeleted);
//    List<BillerProductBundle> findAllByNameAndBillerProductAndIsActiveAndIsDeleted(String name, BillerProduct billerProduct, boolean isActive, boolean isDeleted);
//    List<BillerProductBundle> findAllByBillerProductAndIsActiveAndIsDeleted(BillerProduct billerProduct, boolean isActive, boolean isDeleted);
//    Page<BillerProductBundle> findAllByBillerProductAndIsActiveAndIsDeleted(BillerProduct billerProduct, boolean isActive, boolean isDeleted, Pageable pageable);
//    Page<BillerProductBundle> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);
//
//}
