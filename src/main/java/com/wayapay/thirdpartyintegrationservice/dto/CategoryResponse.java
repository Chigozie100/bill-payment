package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CategoryResponse {

    private String categoryId;
    private String categoryName;
    private String categoryWayapayCode;

    public CategoryResponse(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
}
