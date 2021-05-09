package com.wayapay.thirdpartyintegrationservice.model;

import com.wayapay.thirdpartyintegrationservice.util.FeeBearer;
import com.wayapay.thirdpartyintegrationservice.util.FeeType;
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
@Table(name = "biller_consumer_fee", uniqueConstraints = {@UniqueConstraint(columnNames = {"third_party_name", "biller"})})
public class BillerConsumerFee extends SuperModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "third_party_name", nullable = false)
    private ThirdPartyNames thirdPartyName;

    @Column(name = "biller", nullable = false)
    private String biller;

    @Column(name = "fee_bearer")
    private FeeBearer feeBearer;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private FeeType feeType;

    @Column(name = "value", nullable = false)
    private BigDecimal value = BigDecimal.ZERO;

    @Column(name = "max_fixed_value_when_percentage", nullable = false)
    private BigDecimal maxFixedValueWhenPercentage = BigDecimal.ZERO;

    private boolean active = true;

}
