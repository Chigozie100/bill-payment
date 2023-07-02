package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BillerProductDto {
    @NotBlank(message = "Name can not be NULL or Blank")
    private String name;
    private String description;
    private Long billerCategoryId;
    private boolean isActive;
    private Boolean hasBundles = Boolean.FALSE;
    private Boolean validateCustomerId = Boolean.FALSE;
}
