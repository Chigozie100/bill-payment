package com.wayapay.thirdpartyintegrationservice.responsehelper;

import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;

import java.util.Date;

public class ResponseObj<T> {
    public Date timeStamp;
    public boolean status;
    public String message;
    public T data;

    public ResponseObj(Date timeStamp, boolean status, String message, T data) {
        super();
        this.timeStamp = timeStamp;
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
