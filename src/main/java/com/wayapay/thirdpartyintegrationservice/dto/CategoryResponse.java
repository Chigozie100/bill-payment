package com.wayapay.thirdpartyintegrationservice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel(value = "Data")
public class CategoryResponse {

    @ApiModelProperty(notes = "category Id", example = "Airtime")
    private String categoryId;

    @ApiModelProperty(notes = "category Name", example = "Airtime")
    private String categoryName;

    @ApiModelProperty(notes = "category Wayapay Code", example = "null")
    private String categoryWayapayCode;

    public CategoryResponse(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
}
