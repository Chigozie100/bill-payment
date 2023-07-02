package com.wayapay.thirdpartyintegrationservice.v2.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "service_provider_biller")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ServiceProviderBiller implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String description;
    private String type;
    @Column(name = "image_logo")
    private String imageLogo;
    @Column(name = "biller_id")
    private String billerId;
    @Column(name = "product_id")
    private String productId;
    @Column(name = "e_postpaid_biller_id")
    private String ePostpaidBillerId;
    @Column(name = "e_postpaid_product_id")
    private String ePostpaidProductId;
    @ManyToOne
    private ServiceProviderCategory serviceProviderCategory;
    @Column(name = "service_provider_id")
    private Long serviceProviderId;
    @Column(name = "prepaid_name")
    private String prepaidName;
    @Column(name = "postpaid_name")
    private String postpaidName;
    @Column(name = "has_product")
    private Boolean hasProduct = Boolean.FALSE;
    @Column(name = "is_prepaid")
    private Boolean isPrepaid = Boolean.FALSE;
    @Column(name = "is_postpaid")
    private Boolean isPostpaid = Boolean.FALSE;
    @Column(name = "is_required_id_verification")
    private Boolean isRequiredIdVerification = Boolean.FALSE;
    @Column(name = "is_active")
    private Boolean isActive = Boolean.TRUE;
    @Column(name = "is_deleted")
    private Boolean isDeleted = Boolean.FALSE;
    @Column(name = "created_by")
    private String createdBy;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "modified_by")
    private String modifiedBy;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt = LocalDateTime.now();
}
