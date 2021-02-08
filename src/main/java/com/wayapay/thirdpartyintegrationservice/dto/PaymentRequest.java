package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentRequest {

    @NotBlank(message = "categoryId is required")
    private String categoryId;

    @NotBlank(message = "billerId is required")
    private String billerId;

    @NotBlank(message = "amount is required")
    @DecimalMin(value = "0.01", message = "minimum amount required is 0.01")
    private BigDecimal amount;

    @NotEmpty(message = "No item provided, AtLeast provide an item for validation")
    private List<ParamNameValue> data = new ArrayList<>();

}
