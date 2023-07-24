package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString @JsonIgnoreProperties(ignoreUnknown = true)
public class Pricing {
    private String monthsPaidFor;
    private String price;
    private String invoicePeriod;

    public Pricing(String price) {
        this.price = price;
    }
}