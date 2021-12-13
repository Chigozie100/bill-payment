package com.wayapay.thirdpartyintegrationservice.service.commission;

import com.wayapay.thirdpartyintegrationservice.util.CommissionType;
import com.wayapay.thirdpartyintegrationservice.util.TransactionType;
import com.wayapay.thirdpartyintegrationservice.util.UserType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

@Data
public class MerchantCommissionTrackerDto {
    private UserType userType;
    private TransactionType transactionType;
    private CommissionType commissionType;
    private Double commissionValue;
    private String commissionPaymentStatus;
    private String userId;
}
