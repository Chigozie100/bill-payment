package com.wayapay.thirdpartyintegrationservice.model;

import com.wayapay.thirdpartyintegrationservice.util.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class TransactionTracker  extends SuperModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer count = 0;

    private String referralCode;

    private String referralCodeOwner;

    @Column(unique = true)
    private String referreeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    private boolean isPaid=false;

}
