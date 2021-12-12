package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class MultipleFormPaymentRequest {
    private List<MultiplePaymentRequest> paymentRequest;
}