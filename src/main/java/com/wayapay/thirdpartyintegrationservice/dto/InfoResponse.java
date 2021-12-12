package com.wayapay.thirdpartyintegrationservice.dto;

import java.util.Date;

public class InfoResponse {
    public Date timeStamp;
    public boolean status;
    public String message;
    public NewWalletResponse data;

    public InfoResponse(Date timeStamp, boolean status, String message, NewWalletResponse data) {
        super();
        this.timeStamp = timeStamp;
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
