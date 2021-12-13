package com.wayapay.thirdpartyintegrationservice.dto;

import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BillerManagementResponse {

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "IKEDC")
    private String name;

    @ApiModelProperty(example = "ikedc")
    private String billerAggregatorCode;

    @ApiModelProperty(example = "null")
    private String billerWayaPayCode;

    @ApiModelProperty(example = "true")
    private boolean active;

    @ApiModelProperty(example = "1")
    private Long categoryId;

    @ApiModelProperty(example = "Electricity")
    private String categoryName;

    @ApiModelProperty(example = "ITEX")
    private ThirdPartyNames aggregatorName;

}
