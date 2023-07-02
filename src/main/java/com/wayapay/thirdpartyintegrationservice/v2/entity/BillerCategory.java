//package com.wayapay.thirdpartyintegrationservice.v2.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.ToString;
//
//import javax.persistence.*;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//
//@Data
//@Entity
//@Table(name = "biller_category")
//@AllArgsConstructor
//@NoArgsConstructor
//@ToString
//public class BillerCategory implements Serializable {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", nullable = false)
//    private Long id;
//    private String name;
//    private String description;
//    private String type;
//    private String imageLogo;
//    private String planName;
//    @ManyToOne
//    private Category category;
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
