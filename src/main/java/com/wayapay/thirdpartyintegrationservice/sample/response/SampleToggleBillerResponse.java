package com.wayapay.thirdpartyintegrationservice.sample.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleToggleBillerResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "true")
    private String status;

    @ApiModelProperty(notes = "message", example = "successfully deactivated")
    private String message;

    @ApiModelProperty(notes = "data", example = " {\n" +
            "    \"id\": 1,\n" +
            "    \"name\": \"IKEDC\",\n" +
            "    \"billerAggregatorCode\": \"ikedc\",\n" +
            "    \"billerWayaPayCode\": null,\n" +
            "    \"active\": true,\n" +
            "    \"categoryId\": 1,\n" +
            "    \"categoryName\": \"Electricity\",\n" +
            "    \"aggregatorName\": \"ITEX\"\n" +
            "  }")
    private String data;
}
