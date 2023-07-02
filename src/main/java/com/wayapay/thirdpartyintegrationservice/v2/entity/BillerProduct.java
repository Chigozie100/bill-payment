//package com.wayapay.thirdpartyintegrationservice.v2.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.ToString;
//
//import javax.persistence.*;
//import java.io.Serializable;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//
//@Data
//@Entity
//@Table(name = "biller_product")
//@AllArgsConstructor
//@NoArgsConstructor
//@ToString
//public class BillerProduct implements Serializable {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", nullable = false)
//    private Long id;
//    private String name;
//    private BigDecimal amount = BigDecimal.ZERO;
//    private String description;
//    @ManyToOne @Column(name = "biller_category")
//    private BillerCategory billerCategory;
//    @Column(name = "has_bundles")
//    private Boolean hasBundles = Boolean.FALSE;
//    @Column(name = "validate_customer_id")
//    private Boolean validateCustomerId = Boolean.FALSE;
//    @Column(name = "is_active")
//    private Boolean isActive = Boolean.TRUE;
//    @Column(name = "is_deleted")
//    private Boolean isDeleted = Boolean.FALSE;
//    @Column(name = "created_by")
//    private String createdBy;
//    @Column(name = "created_at")
//    private LocalDateTime createdAt = LocalDateTime.now();
//    @Column(name = "modified_by")
//    private String modifiedBy;
//    @Column(name = "modified_at")
//    private LocalDateTime modifiedAt = LocalDateTime.now();
//
//}
