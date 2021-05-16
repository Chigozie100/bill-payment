package com.wayapay.thirdpartyintegrationservice.dto;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "Airtime", required = true)
    @NotBlank(message = "name is required")
    @Size(min = 3, max = 200, message = "name should be between 3 and 200 characters")
    @Pattern(regexp = "[a-zA-Z0-9-' ]*", message = "Please enter a valid name")
    private String name;

    @ApiModelProperty(example = "Airtime", required = true)
    @NotBlank(message = "categoryAggregatorCode is required")
    @Size(min = 3, max = 200, message = "categoryAggregatorCode should be between 3 and 200 characters")
    private String categoryAggregatorCode;

    @ApiModelProperty(example = "WayaPay Airtime")
    private String categoryWayaPayCode;

    @ApiModelProperty(example = "1", required = true)
    @NotNull(message = "aggregator Id is required")
    private Long aggregatorId;

}
