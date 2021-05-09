package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryManagementRequest {

    private Long id;

    @NotBlank(message = "name is required")
    @Size(min = 3, max = 200, message = "name should be between 3 and 200 characters")
    @Pattern(regexp = "[a-zA-Z0-9-' ]*", message = "Please enter a valid name")
    private String name;

    @NotBlank(message = "categoryAggregatorCode is required")
    @Size(min = 3, max = 200, message = "categoryAggregatorCode should be between 3 and 200 characters")
    private String categoryAggregatorCode;

    private String categoryWayaPayCode;

    @NotNull(message = "aggregator Id is required")
    private Long aggregatorId;

}
