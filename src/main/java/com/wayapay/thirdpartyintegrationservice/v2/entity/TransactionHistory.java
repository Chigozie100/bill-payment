package com.wayapay.thirdpartyintegrationservice.v2.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapay.thirdpartyintegrationservice.v2.dto.BillCategoryName;
import com.wayapay.thirdpartyintegrationservice.v2.dto.PaymentStatus;
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
@Table(name = "transaction_history")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransactionHistory implements Serializable {
    @Version
    private Long version;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal fee = BigDecimal.ZERO;
    @Column(name = "account_number")
    private String accountNumber;
    @Column(name = "sender_name")
    private String senderName;
    @Column(name = "sender_user_id")
    private String senderUserId;
    private String narration;
    @Column(name = "customer_data_token")
    private String customerDataToken;
    @Column(name = "payment_reference_number", unique = true)
    private String paymentReferenceNumber;
    @Column(name = "service_provider_reference_number",unique = true)
    private String serviceProviderReferenceNumber;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Enumerated(EnumType.STRING) @Column(name = "category_name")
    private BillCategoryName categoryName;
    @ManyToOne
    private ServiceProviderBiller serviceProviderBiller;
    @ManyToOne
    private ServiceProviderProductBundle serviceProviderProductBundle;
    @ManyToOne
    private ServiceProviderProduct serviceProviderProduct;
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
