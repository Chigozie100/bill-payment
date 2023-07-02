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
@Table(name = "service_provider_product")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ServiceProviderProduct implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String description;
    private String type;
    @Column(name = "product_code")
    private String productCode;
    @Column(name = "has_bundle")
    private Boolean hasBundle = Boolean.FALSE;
    @Column(name = "has_token_validation")
    private Boolean hasTokenValidation = Boolean.FALSE;
    @Column(name = "has_add_ons")
    private Boolean hasAddOns = Boolean.FALSE;
    @ManyToOne
    private ServiceProviderBiller serviceProviderBiller;
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
