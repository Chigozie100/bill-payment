package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.Data;


@Data
public class UpdateServiceProvider {
    private int precedence;
    private Boolean processEpin = Boolean.FALSE;
    private Boolean processCableTv = Boolean.FALSE;
    private Boolean processAirlineTicket = Boolean.FALSE;
    private Boolean processElectricity = Boolean.FALSE;
    private Boolean processAirtime = Boolean.FALSE;
    private Boolean processDataBundle = Boolean.FALSE;
    private Boolean processBetting = Boolean.FALSE;
    private Boolean processGovernmentPayment = Boolean.FALSE;
    private Boolean processInsurance = Boolean.FALSE;
    private Boolean processSchoolFees = Boolean.FALSE;
    private Boolean processVisaFees = Boolean.FALSE;
    private Boolean processTaxesLevies = Boolean.FALSE;
    private Boolean processInternetSubscription = Boolean.FALSE;
    private Boolean processTithesDonation = Boolean.FALSE;
    private Boolean processEducation = Boolean.FALSE;
    private Boolean processVehicle = Boolean.FALSE;
    private Boolean processTransport = Boolean.FALSE;
    private Boolean processEmbassy = Boolean.FALSE;
    private Boolean processSchoolBoard = Boolean.FALSE;
    private Boolean processShopping = Boolean.FALSE;
    private Boolean processEventTicket = Boolean.FALSE;
    private Boolean processOnlineShopping = Boolean.FALSE;
    private Boolean processInsuranceInvestment = Boolean.FALSE;
    private Boolean processInternationalAirtime = Boolean.FALSE;
    private Boolean processLagosStateCBS = Boolean.FALSE;
    private Boolean processCreditLoanRepayment = Boolean.FALSE;
    private Boolean processPayTvSubscription = Boolean.FALSE;
    private Boolean processReligiousInstitutions = Boolean.FALSE;
    private Boolean processNestleDistributors = Boolean.FALSE;
    private Boolean processBlackFriday = Boolean.FALSE;
    private Boolean processApmTerminals = Boolean.FALSE;
    private Boolean processDealerPayments = Boolean.FALSE;
}
