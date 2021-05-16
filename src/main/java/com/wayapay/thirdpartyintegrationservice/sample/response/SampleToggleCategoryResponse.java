package com.wayapay.thirdpartyintegrationservice.sample.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleToggleCategoryResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "true")
    private String status;

    @ApiModelProperty(notes = "message", example = "successfully deactivated")
    private String message;

    @ApiModelProperty(notes = "data", example = " {\n" +
            "    \"id\": 1,\n" +
            "    \"name\": \"Electricity\",\n" +
            "    \"categoryAggregatorCode\": \"Electricity\",\n" +
            "    \"categoryWayaPayCode\": null,\n" +
            "    \"active\": false,\n" +
            "    \"aggregatorId\": 1,\n" +
            "    \"aggregatorName\": \"ITEX\"\n" +
            "  }")
    private String data;
}
