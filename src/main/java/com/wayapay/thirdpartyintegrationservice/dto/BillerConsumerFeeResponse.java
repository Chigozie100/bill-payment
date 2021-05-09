package com.wayapay.thirdpartyintegrationservice.dto;

import com.wayapay.thirdpartyintegrationservice.model.BillerConsumerFee;
import com.wayapay.thirdpartyintegrationservice.util.FeeBearer;
import com.wayapay.thirdpartyintegrationservice.util.FeeType;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BillerConsumerFeeResponse {

    private Long id;

    private ThirdPartyNames thirdPartyName;

    private String biller;

    private FeeType feeType;

    private FeeBearer feeBearer;

    private BigDecimal value;

    private BigDecimal maxFixedValueWhenPercentage;

    private Boolean active;

    public BillerConsumerFeeResponse(BillerConsumerFee billerConsumerFee) {
        if (Objects.isNull(billerConsumerFee)){
            return;
        }
        this.id = billerConsumerFee.getId();
        this.thirdPartyName = billerConsumerFee.getThirdPartyName();
        this.biller = billerConsumerFee.getBiller();
        this.feeType = billerConsumerFee.getFeeType();
        this.feeBearer = billerConsumerFee.getFeeBearer();
        this.value = billerConsumerFee.getValue();
        this.maxFixedValueWhenPercentage = billerConsumerFee.getMaxFixedValueWhenPercentage();
        this.active = billerConsumerFee.isActive();
    }

    public BillerConsumerFeeResponse(Long id, ThirdPartyNames thirdPartyName, String biller, FeeType feeType, FeeBearer feeBearer, BigDecimal value, BigDecimal maxFixedValueWhenPercentage, Boolean active) {
        this.id = id;
        this.thirdPartyName = thirdPartyName;
        this.biller = biller;
        this.feeType = feeType;
        this.feeBearer = feeBearer;
        this.value = value;
        this.maxFixedValueWhenPercentage = maxFixedValueWhenPercentage;
        this.active = active;
    }
}
