package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString @JsonIgnoreProperties(ignoreUnknown = true)
public class Plan {
    private String code;
    private String name;
    private String description;
    private List<Pricing> availablePricingOptions = new ArrayList<>();
}
