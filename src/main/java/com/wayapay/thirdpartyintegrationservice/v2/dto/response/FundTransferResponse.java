package com.wayapay.thirdpartyintegrationservice.v2.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FundTransferResponse {
    private String timeStamp;
    private Boolean status;
    private String message;
    private Object data;

    public FundTransferResponse(Boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}