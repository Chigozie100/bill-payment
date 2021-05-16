package com.wayapay.thirdpartyintegrationservice.dto;

import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryManagementResponse {

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "Airtime")
    private String name;

    @ApiModelProperty(example = "Airtime")
    private String categoryAggregatorCode;

    @ApiModelProperty(example = "WayaPay Airtime")
    private String categoryWayaPayCode;

    @ApiModelProperty(example = "true")
    private boolean active;

    @ApiModelProperty(example = "1")
    private Long aggregatorId;

    @ApiModelProperty(example = "ITEX")
    private ThirdPartyNames aggregatorName;

}
