package com.wayapay.thirdpartyintegrationservice.responsehelper;

import lombok.Getter;

import java.util.Date;

@Getter
public class ResponseHelper {
    private Date timeStamp = new Date();
    private Boolean status;
    private String message;
    private Object data;

    public ResponseHelper(Boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
