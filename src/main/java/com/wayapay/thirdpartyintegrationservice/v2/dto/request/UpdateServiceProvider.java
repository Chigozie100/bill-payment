package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.Data;


@Data
public class UpdateServiceProvider {
    private Boolean processEpin = Boolean.FALSE;
    private Boolean processCableTv = Boolean.FALSE;
    private Boolean processAirlineTicket = Boolean.FALSE;
    private Boolean processUtilityBill = Boolean.FALSE;
    private Boolean processAirtime = Boolean.FALSE;
    private Boolean processDataBundle = Boolean.FALSE;
    private Boolean processBetting = Boolean.FALSE;
    private Boolean processGovernmentPayment = Boolean.FALSE;
    private Boolean processSchoolFees = Boolean.FALSE;
    private Boolean processVisaFees = Boolean.FALSE;
    private Boolean processTaxesAndLevies = Boolean.FALSE;
    private Boolean processInternetSubscription = Boolean.FALSE;
    private Boolean processTithesAndDonation = Boolean.FALSE;
}
