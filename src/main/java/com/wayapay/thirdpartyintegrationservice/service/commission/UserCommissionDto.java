package com.wayapay.thirdpartyintegrationservice.service.commission;

import com.wayapay.thirdpartyintegrationservice.util.CommissionType;
import com.wayapay.thirdpartyintegrationservice.util.TransactionType;
import com.wayapay.thirdpartyintegrationservice.util.UserType;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserCommissionDto {
    private Long id;
    private UserType userType;
    private String category;
    private TransactionType transactionType;
    private CommissionType commissionType;
    private BigDecimal commissionValue;
    private BigDecimal maxFixedValueWhenPercentage;
    private boolean active;

}