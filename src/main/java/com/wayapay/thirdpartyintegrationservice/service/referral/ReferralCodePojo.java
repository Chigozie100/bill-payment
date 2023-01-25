package com.wayapay.thirdpartyintegrationservice.service.referral;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wayapay.thirdpartyintegrationservice.service.profile.Profile;

import lombok.Data;

import java.util.Date;

@Data
public class ReferralCodePojo {
    private String id;
    private String referralCode;
    private String userId;
    private Profile profile;

}
