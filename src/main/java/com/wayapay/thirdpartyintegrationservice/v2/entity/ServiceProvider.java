package com.wayapay.thirdpartyintegrationservice.v2.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "service_provider")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ServiceProvider implements Serializable {
    @Version
    private Long version;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String description;
    private int precedence;
    @Column(name = "process_epin")
    private Boolean processEpin = Boolean.FALSE;
    @Column(name = "process_cable_tv")
    private Boolean processCableTv = Boolean.FALSE;
    @Column(name = "process_airline_ticket")
    private Boolean processAirlineTicket = Boolean.FALSE;
    @Column(name = "process_electricity")
    private Boolean processElectricity = Boolean.FALSE;
    @Column(name = "process_airtime")
    private Boolean processAirtime = Boolean.FALSE;
    @Column(name = "process_data_bundle")
    private Boolean processDataBundle = Boolean.FALSE;
    @Column(name = "process_betting")
    private Boolean processBetting = Boolean.FALSE;
    @Column(name = "process_government_payment")
    private Boolean processGovernmentPayment = Boolean.FALSE;
    @Column(name = "process_insurance")
    private Boolean processInsurance = Boolean.FALSE;
    @Column(name = "process_school_fees")
    private Boolean processSchoolFees = Boolean.FALSE;
    @Column(name = "process_visa_fees")
    private Boolean processVisaFees = Boolean.FALSE;
    @Column(name = "process_taxes_levies")
    private Boolean processTaxesLevies = Boolean.FALSE;
    @Column(name = "process_internet_subscription")
    private Boolean processInternetSubscription = Boolean.FALSE;
    @Column(name = "process_tithes_donation")
    private Boolean processTithesDonation = Boolean.FALSE;
    @Column(name = "process_education")
    private Boolean processEducation = Boolean.FALSE;
    @Column(name = "process_vehicle")
    private Boolean processVehicle = Boolean.FALSE;
    @Column(name = "process_transport")
    private Boolean processTransport = Boolean.FALSE;
    @Column(name = "process_embassy")
    private Boolean processEmbassy = Boolean.FALSE;
    @Column(name = "process_school_board")
    private Boolean processSchoolBoard = Boolean.FALSE;
    @Column(name = "process_shopping")
    private Boolean processShopping = Boolean.FALSE;
    @Column(name = "process_event_ticket")
    private Boolean processEventTicket = Boolean.FALSE;
    @Column(name = "process_online_shopping")
    private Boolean processOnlineShopping = Boolean.FALSE;
    @Column(name = "process_insurance_investment")
    private Boolean processInsuranceInvestment = Boolean.FALSE;
    @Column(name = "process_international_airtime")
    private Boolean processInternationalAirtime = Boolean.FALSE;
    @Column(name = "process_lagos_state_cbs")
    private Boolean processLagosStateCBS = Boolean.FALSE;
    @Column(name = "process_credit_loan_repayment")
    private Boolean processCreditLoanRepayment = Boolean.FALSE;
    @Column(name = "process_pay_tv_subscription")
    private Boolean processPayTvSubscription = Boolean.FALSE;
    @Column(name = "process_religious_institutions")
    private Boolean processReligiousInstitutions = Boolean.FALSE;
    @Column(name = "process_nestle_distributors")
    private Boolean processNestleDistributors = Boolean.FALSE;
    @Column(name = "process_black_friday")
    private Boolean processBlackFriday = Boolean.FALSE;
    @Column(name = "process_apm_terminals")
    private Boolean processApmTerminals = Boolean.FALSE;
    @Column(name = "process_dealer_payments")
    private Boolean processDealerPayments = Boolean.FALSE;
    @Column(name = "is_active")
    private Boolean isActive = Boolean.FALSE;
    @Column(name = "is_deleted")
    private Boolean isDeleted = Boolean.FALSE;
    @Column(name = "created_by")
    private String createdBy;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "modified_by")
    private String modifiedBy;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt = LocalDateTime.now();
}
