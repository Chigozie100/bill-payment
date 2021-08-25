package com.wayapay.thirdpartyintegrationservice.sample.response;

import com.wayapay.thirdpartyintegrationservice.util.Constants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleErrorResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "false")
    private String status;

    @ApiModelProperty(notes = "message", example = Constants.ERROR_MESSAGE)
    private String message;

    @ApiModelProperty(notes = "data", example = "null")
    private String data;
}
