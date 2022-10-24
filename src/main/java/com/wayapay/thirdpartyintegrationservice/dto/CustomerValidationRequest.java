package com.wayapay.thirdpartyintegrationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CustomerValidationRequest {

    @ApiModelProperty(example = "Airtime")
    @NotBlank(message = "categoryId is required")
    private String categoryId;

    @ApiModelProperty(example = "mtnvtu")
    @NotBlank(message = "billerId is required")
    private String billerId;

    @ApiModelProperty(example = "[\n" +
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
