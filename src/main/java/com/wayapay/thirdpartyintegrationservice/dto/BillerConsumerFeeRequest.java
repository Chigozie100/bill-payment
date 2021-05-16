package com.wayapay.thirdpartyintegrationservice.dto;

import com.wayapay.thirdpartyintegrationservice.exceptionhandling.EnumConstraint;
import com.wayapay.thirdpartyintegrationservice.util.FeeBearer;
import com.wayapay.thirdpartyintegrationservice.util.FeeType;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BillerConsumerFeeRequest {

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "QUICKTELLER")
    @NotNull(message = "thirdPartyName is required")
    @EnumConstraint(message = "Invalid thirdPartyName provided, thirdPartyName is either QUICKTELLER, ITEX or BAXI ", enumClass = ThirdPartyNames.class)
    private String thirdPartyName;

    @ApiModelProperty(example = "mtn")
    @NotBlank(message = "biller is required")
    private String biller;

    @ApiModelProperty(example = "FIXED")
    @NotNull
    @EnumConstraint(message = "Invalid feeType provided, feeType is either FIXED or PERCENTAGE ", enumClass = FeeType.class)
    private String feeType;

    @ApiModelProperty(example = "BILLER")
    @NotNull
    @EnumConstraint(message = "Invalid feeBearer provided, feeBearer is either BILLER or CONSUMER ", enumClass = FeeBearer.class)
    private String feeBearer;

    @ApiModelProperty(example = "10.00")
    @NotNull(message = "value is required ")
    @DecimalMin(value = "0.00", message = "Minimum commission value is 0.00")
    private BigDecimal value;

    @ApiModelProperty(example = "0")
    @NotNull(message = "maxFixedValueWhenPercentage is required ")
    @DecimalMin(value = "0.00", message = "Minimum maxFixedValueWhenPercentage value is 0.00")
    private BigDecimal maxFixedValueWhenPercentage;

}
