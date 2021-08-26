package com.wayapay.thirdpartyintegrationservice.dto;

import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;

import java.util.Date;

public class InfoResponse {
    public Date timeStamp;
    public boolean status;
    public String message;
    public NewWalletResponse data;
    public UserProfileResponse data2;

    public InfoResponse(Date timeStamp, boolean status, String message, NewWalletResponse data) {
        super();
        this.timeStamp = timeStamp;
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
