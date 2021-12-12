package com.wayapay.thirdpartyintegrationservice.service.commission;

import com.wayapay.thirdpartyintegrationservice.util.TransactionType;
import com.wayapay.thirdpartyintegrationservice.util.UserType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionDto {
    private String description;
    private String userId;
    private boolean isResolved;
    private boolean successful;
    private Object jsonRequest;
    private Object jsonResponse;
    private UserType userType;
    private BigDecimal commissionValue = BigDecimal.ZERO;
    private TransactionType transactionType;

}
