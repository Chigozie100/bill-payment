package com.wayapay.thirdpartyintegrationservice.sample.response;

import com.wayapay.thirdpartyintegrationservice.dto.BillerConsumerFeeResponse;
import com.wayapay.thirdpartyintegrationservice.dto.BillerManagementResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SampleBillerFeeManagementResponse {

    @ApiModelProperty(notes = "timeStamp", example = "2021-05-16 11:28:45")
    private String timeStamp;

    @ApiModelProperty(notes = "status", example = "true")
    private String status;

    @ApiModelProperty(notes = "message", example = "Successful")
    private String message;

    @ApiModelProperty(notes = "data")
    private BillerConsumerFeeResponse data;
}
