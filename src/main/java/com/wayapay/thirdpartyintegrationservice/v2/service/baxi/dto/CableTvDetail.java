package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CableTvDetail {
    private List<CableTvPlan> availablePricingOptions;
    private String code;
    private String name;
    private String description;
}
