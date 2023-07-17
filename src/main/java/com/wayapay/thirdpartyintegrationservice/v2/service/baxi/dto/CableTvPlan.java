package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CableTvPlan {
    private String monthsPaidFor;
    private String price;
    private String invoicePeriod;
}
