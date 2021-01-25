package com.wayapay.thirdpartyintegrationservice.dto;

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
public class CustomerValidationResponse {
    private String categoryId;
    private String billerId;
    private List<Item> items = new ArrayList<>();
    private List<ParamNameValue> data = new ArrayList<>();

    public CustomerValidationResponse(String categoryId, String billerId) {
        this.categoryId = categoryId;
        this.billerId = billerId;
    }
}
