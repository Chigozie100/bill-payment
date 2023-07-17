package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @JsonIgnoreProperties(ignoreUnknown = true) @NoArgsConstructor @AllArgsConstructor
public class ErrorQtDto {
    private ErrorPaymentResp error;
    private List<ErrorPaymentResp> errors;
}
