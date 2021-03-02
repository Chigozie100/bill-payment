package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class GetAllBillersByCategoryResponse {
    private List<BillerDetail> billers = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class BillerDetail{
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
}