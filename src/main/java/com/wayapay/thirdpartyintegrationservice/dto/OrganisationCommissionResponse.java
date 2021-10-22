package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
public class OrganisationCommissionResponse {
    private Long id;
    private String organisationType;
    private String transactionType;
    private String biller;
    private String commissionType;
    private Double commissionValue;
    private Double maxFixedValueWhenPercentage;
    private Integer genCommToCorpAccount;
    private Integer commissionToAgent;
    private Integer commissionToAgentsAggregator;
    private String merchantName;
    private String corporateUserId;
    private String corporateUserPhoneNumber;
    private String corporateUserEmail;
    private Integer generalCommission;
    private boolean active = true;
}
