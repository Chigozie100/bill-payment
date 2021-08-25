package com.wayapay.thirdpartyintegrationservice.sample.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleListBillerManagementResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "true")
    private String status;

    @ApiModelProperty(notes = "message", example = "Successful")
    private String message;

    @ApiModelProperty(notes = "data", example = "[\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"name\": \"IKEDC\",\n" +
            "      \"billerAggregatorCode\": \"ikedc\",\n" +
            "      \"billerWayaPayCode\": null,\n" +
            "      \"active\": true,\n" +
            "      \"categoryId\": 1,\n" +
            "      \"categoryName\": \"Electricity\",\n" +
            "      \"aggregatorName\": \"ITEX\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 2,\n" +
            "      \"name\": \"AEDC\",\n" +
            "      \"billerAggregatorCode\": \"aedc\",\n" +
            "      \"billerWayaPayCode\": null,\n" +
            "      \"active\": true,\n" +
            "      \"categoryId\": 1,\n" +
            "      \"categoryName\": \"Electricity\",\n" +
            "      \"aggregatorName\": \"ITEX\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 3,\n" +
            "      \"name\": \"PHEDC\",\n" +
            "      \"billerAggregatorCode\": \"phedc\",\n" +
            "      \"billerWayaPayCode\": null,\n" +
            "      \"active\": true,\n" +
            "      \"categoryId\": 1,\n" +
            "      \"categoryName\": \"Electricity\",\n" +
            "      \"aggregatorName\": \"ITEX\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 4,\n" +
            "      \"name\": \"EEDC\",\n" +
            "      \"billerAggregatorCode\": \"eedc\",\n" +
            "      \"billerWayaPayCode\": null,\n" +
            "      \"active\": true,\n" +
            "      \"categoryId\": 1,\n" +
            "      \"categoryName\": \"Electricity\",\n" +
            "      \"aggregatorName\": \"ITEX\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 5,\n" +
            "      \"name\": \"IBEDC\",\n" +
            "      \"billerAggregatorCode\": \"ibedc\",\n" +
            "      \"billerWayaPayCode\": null,\n" +
            "      \"active\": true,\n" +
            "      \"categoryId\": 1,\n" +
            "      \"categoryName\": \"Electricity\",\n" +
            "      \"aggregatorName\": \"ITEX\"\n" +
            "    }\n" +
            "]")
    private String data;
}
