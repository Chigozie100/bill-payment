package com.wayapay.thirdpartyintegrationservice.dto;

import com.wayapay.thirdpartyintegrationservice.model.BillerConsumerFee;
import com.wayapay.thirdpartyintegrationservice.util.FeeBearer;
import com.wayapay.thirdpartyintegrationservice.util.FeeType;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "QUICKTELLER")
    private ThirdPartyNames thirdPartyName;

    @ApiModelProperty(example = "mtn")
    private String biller;

    @ApiModelProperty(example = "FIXED")
    private FeeType feeType;

    @ApiModelProperty(example = "BILLER")
    private FeeBearer feeBearer;

    @ApiModelProperty(example = "10")
    private BigDecimal value;

    @ApiModelProperty(example = "0")
    private BigDecimal maxFixedValueWhenPercentage;

    @ApiModelProperty(example = "true")
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
