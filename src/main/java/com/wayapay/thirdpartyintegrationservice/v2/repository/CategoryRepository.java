package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndIsDeleted(Long id, boolean isDeleted);
    Optional<Category> findByIdAndIsActiveAndIsDeleted(Long id,boolean isActive, boolean isDeleted);
    Optional<Category> findByNameAndIsActiveAndIsDeleted(String name,boolean isActive, boolean isDeleted);
    Optional<Category> findByNameAndIsDeleted(String name, boolean isDeleted);
    Page<Category> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);
    List<Category> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted);

    List<Category> findAllByIsDeleted(boolean isDeleted);
}
