//package com.wayapay.thirdpartyintegrationservice.v2.repository;
//
//import com.wayapay.thirdpartyintegrationservice.v2.entity.BillerCategory;
//import com.wayapay.thirdpartyintegrationservice.v2.entity.Category;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface BillerCategoryRepository extends JpaRepository<BillerCategory,Long> {
//    Optional<BillerCategory> findByNameAndIsActiveAndIsDeleted(String name, boolean isActive, boolean isDeleted);
//    Optional<BillerCategory> findByNameAndCategoryAndIsActiveAndIsDeleted(String name, Category category, boolean isActive, boolean isDeleted);
//    Optional<BillerCategory> findByIdAndCategoryAndIsActiveAndIsDeleted(Long id, Category category, boolean isActive, boolean isDeleted);
//    Optional<BillerCategory> findByIdAndNameAndIsActiveAndIsDeleted(Long id,String name, Category category, boolean isActive, boolean isDeleted);
//    Page<BillerCategory> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);
//    List<BillerCategory> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted);
//    List<BillerCategory> findAllByCategoryAndIsActiveAndIsDeleted(Category category,boolean isActive, boolean isDeleted);
//    Page<BillerCategory> findAllByCategoryAndIsActiveAndIsDeleted(Category category,boolean isActive, boolean isDeleted,Pageable pageable);
//    Optional<BillerCategory> findByIdAndIsActiveAndIsDeleted(Long id, boolean isActive, boolean isDeleted);
//}
