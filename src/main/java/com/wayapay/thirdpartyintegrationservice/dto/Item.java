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
public class Item {
    private String paramName;
    private Boolean isAmountFixed = Boolean.FALSE;
    private List<SubItem> subItems = new ArrayList<>();

    public Item(String paramName) {
        this.paramName = paramName;
    }
}