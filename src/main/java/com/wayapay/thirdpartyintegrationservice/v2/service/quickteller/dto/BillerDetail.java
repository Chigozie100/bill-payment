package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BillerDetail {
    private String categoryid;
    private String categoryname;
    private String categorydescription;
    private String billerid;
    private String billername;
    private String customerfield1;
    private String customerfield2;
    private String supportemail;
    private String paydirectProductId;
    private String paydirectInstitutionId;
    private String narration;
    private String shortName;
    private String surcharge;
    private String currencyCode;
    private String quickTellerSiteUrlName;
    private String amountType;
    private String currencySymbol;
    private String customSectionUrl;
    private String logoUrl;
    private String type;
    private String url;
    private String customerId;
    private String customerEmail;
    private String productCode;
    private String customerMobile;


//    private String customerId = "customerId";
//    private String customerEmail = "customerEmail";
//    private String paymentCode = "paymentCode";
//    private String customerMobile = "customerMobile";
}
