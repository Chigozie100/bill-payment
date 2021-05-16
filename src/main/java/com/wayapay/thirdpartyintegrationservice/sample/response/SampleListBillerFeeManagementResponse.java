package com.wayapay.thirdpartyintegrationservice.sample.response;

import com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleListBillerFeeManagementResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "true")
    private String status;

    @ApiModelProperty(notes = "message", example = "Successful")
    private String message;

    @ApiModelProperty(notes = "data", example = " [\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"thirdPartyName\": \"QUICKTELLER\",\n" +
            "      \"biller\": \"mtn\",\n" +
            "      \"feeType\": \"FIXED\",\n" +
            "      \"feeBearer\": BILLER,\n" +
            "      \"value\": 10,\n" +
            "      \"maxFixedValueWhenPercentage\": 0,\n" +
            "      \"active\": false\n" +
            "    }\n" +
            "  ]")
    private String data;
}
