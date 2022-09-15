package com.wayapay.thirdpartyintegrationservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentRequest {

    @ApiModelProperty(example = "Airtime")
    @NotBlank(message = "categoryId is required")
    private String categoryId;

    @ApiModelProperty(example = "mtnvtu")
    @NotBlank(message = "billerId is required")
    private String billerId;

    @ApiModelProperty(example = "2011114160")
    @NotBlank(message = "source wallet account number is required")
    private String sourceWalletAccountNumber;

    @ApiModelProperty(example = "50")
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "minimum amount required is 0.01")
    private BigDecimal amount;

    @ApiModelProperty(example = " [\n" +
            "    {\n" +
            "      \"name\": \"phone\",\n" +
            "      \"value\": \"+2348189747520\"\n" +
            "    },\n" +
            "\t{\n" +
            "      \"name\": \"amount\",\n" +
            "      \"value\": \"50\"\n" +
            "    },\n" +
            "\t{\n" +
            "      \"name\": \"paymentMethod\",\n" +
            "      \"value\": \"cash\"\n" +
            "    },\n" +
            "\t{\n" +
            "      \"name\": \"channel\",\n" +
            "      \"value\": \"ATM\"\n" +
            "    }\n" +
            "  ]")
    @NotEmpty(message = "No item provided, AtLeast provide an item for validation")
    private List<ParamNameValue> data = new ArrayList<>();

}
