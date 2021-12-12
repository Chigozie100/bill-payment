package com.wayapay.thirdpartyintegrationservice.dto;

import com.wayapay.thirdpartyintegrationservice.service.profile.UserProfileResponse;

import java.util.Date;

public class ProfileResponseObject {
    public Date timeStamp;
    public boolean status;
    public String message;
    public UserProfileResponse data;

    public ProfileResponseObject(Date timeStamp, boolean status, String message, UserProfileResponse data) {
        super();
        this.timeStamp = timeStamp;
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
