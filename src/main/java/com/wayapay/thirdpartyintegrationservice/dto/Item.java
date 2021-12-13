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
public class Item {

    @ApiModelProperty(notes = "paramName")
    private String paramName;

    @ApiModelProperty(notes = "isAmountFixed")
    private Boolean isAmountFixed = Boolean.FALSE;

    @ApiModelProperty(notes = "subItem")
    private List<SubItem> subItems = new ArrayList<>();

    public Item(String paramName) {
        this.paramName = paramName;
    }
}