package com.wayapay.thirdpartyintegrationservice.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="transaction_log")
public class TransactionLog extends SuperModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String requestReference;

    private String thirdParty;

    private String statusCode;

    private String statusMessage;

    @Column(columnDefinition = "TEXT")
    private String paymentRequest;

    @Column(columnDefinition = "TEXT")
    private String paymentResponse;

    @Column(columnDefinition = "TEXT")
    private String errorResponse;
}
