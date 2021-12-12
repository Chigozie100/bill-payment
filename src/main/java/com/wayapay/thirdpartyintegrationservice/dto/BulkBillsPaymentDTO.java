package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkBillsPaymentDTO {
    @NotEmpty(message= "List Should Not be Empty")
    private List<PaymentRequestExcel> paymentRequestExcels;
}
