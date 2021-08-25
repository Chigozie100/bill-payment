package com.wayapay.thirdpartyintegrationservice.sample.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SamplePaymentResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "true")
    private String status;

    @ApiModelProperty(notes = "message", example = "Successful")
    private String message;

    @ApiModelProperty(notes = "data", example = "{\n" +
            "    \"data\": [\n" +
            "      {\n" +
            "        \"name\": \"reference\",\n" +
            "        \"value\": \"WAYA-MTN20210516152845HRJ84948\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }")
    private String data;
}
