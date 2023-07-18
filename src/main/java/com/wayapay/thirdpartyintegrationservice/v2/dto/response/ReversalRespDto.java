package com.wayapay.thirdpartyintegrationservice.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class ReversalRespDto {
    private Date timeStamp;
    private boolean status;
    private String message;
    private Object data;
}
