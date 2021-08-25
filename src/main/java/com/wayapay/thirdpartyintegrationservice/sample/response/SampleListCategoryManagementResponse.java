package com.wayapay.thirdpartyintegrationservice.sample.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleListCategoryManagementResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "true")
    private String status;

    @ApiModelProperty(notes = "message", example = "Successful")
    private String message;

    @ApiModelProperty(notes = "data", example = "[\n" +
            "    {\n" +
            "      \"id\": 7,\n" +
            "      \"name\": \"LCC\",\n" +
            "      \"categoryAggregatorCode\": \"LCC\",\n" +
            "      \"categoryWayaPayCode\": null,\n" +
            "      \"active\": true,\n" +
            "      \"aggregatorId\": 1,\n" +
            "      \"aggregatorName\": \"ITEX\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 6,\n" +
            "      \"name\": \"Remita\",\n" +
            "      \"categoryAggregatorCode\": \"Remita\",\n" +
            "      \"categoryWayaPayCode\": null,\n" +
            "      \"active\": true,\n" +
            "      \"aggregatorId\": 1,\n" +
            "      \"aggregatorName\": \"ITEX\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 5,\n" +
            "      \"name\": \"Internet\",\n" +
            "      \"categoryAggregatorCode\": \"Internet\",\n" +
            "      \"categoryWayaPayCode\": null,\n" +
            "      \"active\": true,\n" +
            "      \"aggregatorId\": 1,\n" +
            "      \"aggregatorName\": \"ITEX\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 4,\n" +
            "      \"name\": \"CableTv\",\n" +
            "      \"categoryAggregatorCode\": \"CableTv\",\n" +
            "      \"categoryWayaPayCode\": null,\n" +
            "      \"active\": true,\n" +
            "      \"aggregatorId\": 1,\n" +
            "      \"aggregatorName\": \"ITEX\"\n" +
            "    }\n" +
            "]")
    private String data;
}
