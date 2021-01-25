package com.wayapay.thirdpartyintegrationservice.dto;

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

    @NotBlank(message = "categoryId is required")
    private String categoryId;

    @NotBlank(message = "billerId is required")
    private String billerId;

    @NotEmpty(message = "No item provided, AtLeast provide an item for validation")
    private List<ParamNameValue> data = new ArrayList<>();
}
