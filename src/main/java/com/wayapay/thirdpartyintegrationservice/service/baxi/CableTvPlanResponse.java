package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CableTvPlanResponse extends SuperResponse {
    private List<Plan> data = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class Plan {
    private String code;
    private String name;
    private String description;
    private List<Pricing> availablePricingOptions = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class Pricing {
    private String monthsPaidFor;
    private String price;
    private String invoicePeriod;

    public Pricing(String price) {
        this.price = price;
    }
}
