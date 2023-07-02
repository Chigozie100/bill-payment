package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;

@Data
public class CategoryDto {
    @NotBlank(message = "Name can not be NULL or Blank")
    private String name;
    private String description;
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
    private Boolean isActive = Boolean.FALSE;
}
