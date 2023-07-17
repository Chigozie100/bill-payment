package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @JsonIgnoreProperties(ignoreUnknown = true)@NoArgsConstructor @AllArgsConstructor
public class ErrorPaymentResp {
    private String code;
    private String message;
    private String responseCodeGrouping;
}
