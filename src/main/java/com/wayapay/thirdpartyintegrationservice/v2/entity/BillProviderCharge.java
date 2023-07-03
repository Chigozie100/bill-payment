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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bill_provider_charges")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BillProviderCharge implements Serializable {

    @Version
    private Long version;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private BigDecimal amount;
    private BigDecimal fees;
    @Column(name = "biller_charges")
    private BigDecimal billerCharges;
    @Column(name = "consumer_charges")
    private BigDecimal consumerCharges;
    @ManyToOne
    private ServiceProviderCategory serviceProviderCategory;
    @ManyToOne
    private ServiceProvider serviceProvider;
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
