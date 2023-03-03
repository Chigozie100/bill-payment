package com.wayapay.thirdpartyintegrationservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BillspaymentCommissionRespones {
    private Long id;
    private Long userId;
    private String fullName;
    private BigDecimal amount;
    private String status; //// CUSTOM  DEFAULT
    private String commissionType;
    private BigDecimal customAmount;
    private String billerCode;
    private String billerName;
    private String billerAggregatorName;
    private String categoryName;
    private String categoryCode;
}
