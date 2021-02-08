package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CableTvAddonsResponse extends SuperResponse {
    private List<CableTvDetail> data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class CableTvDetail {
    private List<CableTvPlan> availablePricingOptions;
    private String code;
    private String name;
    private String description;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class CableTvPlan {
    private String monthsPaidFor;
    private String price;
    private String invoicePeriod;
}