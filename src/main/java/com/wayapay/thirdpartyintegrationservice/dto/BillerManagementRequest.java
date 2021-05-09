package com.wayapay.thirdpartyintegrationservice.dto;

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

    private Long id;

    @NotBlank(message = "name is required")
    @Size(min = 3, max = 200, message = "name should be between 3 and 200 characters")
    @Pattern(regexp = "[a-zA-Z0-9-' ]*", message = "Please enter a valid name")
    private String name;

    @NotBlank(message = "billerAggregatorCode is required")
    @Size(min = 3, max = 200, message = "billerAggregatorCode should be between 3 and 200 characters")
    private String billerAggregatorCode;

    private String billerWayaPayCode;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

}
