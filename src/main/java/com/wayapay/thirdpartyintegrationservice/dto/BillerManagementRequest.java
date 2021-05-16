package com.wayapay.thirdpartyintegrationservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BillerManagementRequest {

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "IKEDC")
    @NotBlank(message = "name is required")
    @Size(min = 3, max = 200, message = "name should be between 3 and 200 characters")
    @Pattern(regexp = "[a-zA-Z0-9-' ]*", message = "Please enter a valid name")
    private String name;

    @ApiModelProperty(example = "ikedc")
    @NotBlank(message = "billerAggregatorCode is required")
    @Size(min = 3, max = 200, message = "billerAggregatorCode should be between 3 and 200 characters")
    private String billerAggregatorCode;

    @ApiModelProperty(example = "null")
    private String billerWayaPayCode;

    @ApiModelProperty(example = "2")
    @NotNull(message = "categoryId is required")
    private Long categoryId;

}
