package com.wayapay.thirdpartyintegrationservice.dto;

import io.swagger.annotations.ApiModelProperty;
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
public class PaymentItemsResponse {

//    @ApiModelProperty(notes = "categoryId", example = "Airtime")
    private String categoryId;

//    @ApiModelProperty(notes = "billerId", example = "mtnvtu")
    private String billerId;

//    @ApiModelProperty(notes = "items")
    private List<Item> items = new ArrayList<>();

//    @ApiModelProperty(notes = "billerId", example = "mtnvtu")
    private Boolean isValidationRequired = Boolean.TRUE;

    public PaymentItemsResponse(String categoryId, String billerId) {
        this.categoryId = categoryId;
        this.billerId = billerId;
    }
}
