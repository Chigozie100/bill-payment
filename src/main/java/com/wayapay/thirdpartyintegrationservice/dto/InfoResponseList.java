package com.wayapay.thirdpartyintegrationservice.dto;

import java.util.Date;
import java.util.List;

public class InfoResponseList {
    public Date timeStamp;
    public boolean status;
    public String message;
    public List<NewWalletResponse> data;

    public InfoResponseList(Date timeStamp, boolean status, String message, List<NewWalletResponse> data) {
        super();
        this.timeStamp = timeStamp;
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
