package com.wayapay.thirdpartyintegrationservice.sample.response;

import com.wayapay.thirdpartyintegrationservice.dto.ThirdPartyResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleToggleAggregatorResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "true")
    private String status;

    @ApiModelProperty(notes = "message", example = "successfully deactivated")
    private String message;

    @ApiModelProperty(notes = "data", example = "{\n" +
            "    \"id\": 1,\n" +
            "    \"aggregator\": \"ITEX\",\n" +
            "    \"active\": false\n" +
            "  }")
    private ThirdPartyResponse data;
}
