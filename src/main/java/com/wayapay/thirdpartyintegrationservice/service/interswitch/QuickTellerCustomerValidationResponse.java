package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class QuickTellerCustomerValidationResponse {

    @JsonProperty("Customers")
    private List<ValidationResponse> Customers = new ArrayList<>();

}