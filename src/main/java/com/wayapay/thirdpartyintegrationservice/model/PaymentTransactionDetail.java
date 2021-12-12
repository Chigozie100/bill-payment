package com.wayapay.thirdpartyintegrationservice.model;

import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class PaymentTransactionDetail extends SuperModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThirdPartyNames thirdPartyName;

    private BigDecimal amount;
    
    private BigDecimal fee;

    private Boolean successful;

    private String category;

    private String biller;

    @Column(columnDefinition = "TEXT")
    private String paymentRequest;

    @Column(columnDefinition = "TEXT")
    private String paymentResponse;

    private String username;

    private String phoneNumber;

    private String referralCode;

    private String email;

    private String channel;

    private String userAccountNumber;

    private boolean isResolved;

}
