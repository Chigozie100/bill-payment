package com.wayapay.thirdpartyintegrationservice.service.referral;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ReferralCodePojo {
    private String id;
    private String referralCode;
    private String userId;
    private String profile;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+1")
    private Date createdAt;


}
