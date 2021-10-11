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
public class BillsPaymentRefund extends SuperModel{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transactionId;

    private BigDecimal amount;

    private boolean successful;

    private String userId;


}
