package com.wayapay.thirdpartyintegrationservice.v2.repository;

import com.wayapay.thirdpartyintegrationservice.v2.entity.ServiceProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    Optional<List<ServiceProvider>> findByNameStartsWith(String prefix);
    Optional<ServiceProvider> findByNameAndIsActiveAndIsDeleted(String name, boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByNameAndIsDeleted(String name, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndIsActiveAndIsDeleted(Long id, boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndIsDeleted(Long id, boolean isDeleted);
    List<ServiceProvider> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted);
    Page<ServiceProvider> findAllByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);
    Optional<ServiceProvider> findByIdAndProcessCableTvAndIsActiveAndIsDeleted(Long id, boolean processCableTv,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessAirlineTicketAndIsActiveAndIsDeleted(Long id, boolean processAirlineTicket,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessElectricityAndIsActiveAndIsDeleted(Long id, boolean processElectricity,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessAirtimeAndIsActiveAndIsDeleted(Long id, boolean processAirtime,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessDataBundleAndIsActiveAndIsDeleted(Long id, boolean processDataBundle,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessBettingAndIsActiveAndIsDeleted(Long id, boolean processBetting,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessGovernmentPaymentAndIsActiveAndIsDeleted(Long id, boolean processGovernmentPayment,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessSchoolFeesAndIsActiveAndIsDeleted(Long id, boolean processSchoolFees,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessVisaFeesAndIsActiveAndIsDeleted(Long id, boolean processVisaFees,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessTaxesLeviesAndIsActiveAndIsDeleted(Long id, boolean processTaxesLevies,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessInternetSubscriptionAndIsActiveAndIsDeleted(Long id, boolean processInternetSubscription,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessTithesDonationAndIsActiveAndIsDeleted(Long id, boolean processTithesDonation,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessEpinAndIsActiveAndIsDeleted(Long id, boolean processEpin,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findByIdAndProcessInsuranceAndIsActiveAndIsDeleted(Long id,boolean processInsurance,boolean isActive, boolean isDeleted);

    Optional<ServiceProvider> findFirstByProcessInsuranceAndIsActiveAndIsDeleted(boolean processInsurance,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessCableTvAndIsActiveAndIsDeleted(boolean processCableTv,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessAirlineTicketAndIsActiveAndIsDeleted(boolean processAirlineTicket,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessElectricityAndIsActiveAndIsDeleted(boolean processElectricity,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessAirtimeAndIsActiveAndIsDeleted(boolean processAirtime,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessDataBundleAndIsActiveAndIsDeleted(boolean processDataBundle,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessBettingAndIsActiveAndIsDeleted(boolean processBetting,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessGovernmentPaymentAndIsActiveAndIsDeleted(boolean processGovernmentPayment,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessSchoolFeesAndIsActiveAndIsDeleted(boolean processSchoolFees,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessVisaFeesAndIsActiveAndIsDeleted(boolean processVisaFees,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessTaxesLeviesAndIsActiveAndIsDeleted(boolean processTaxesLevies,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessInternetSubscriptionAndIsActiveAndIsDeleted(boolean processInternetSubscription,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessTithesDonationAndIsActiveAndIsDeleted(boolean processTithesDonation,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByProcessEpinAndIsActiveAndIsDeleted(boolean processEpin,boolean isActive, boolean isDeleted);

    Optional<ServiceProvider> findFirstByPrecedenceAndProcessEpinAndIsActiveAndIsDeleted(int precedence,boolean processEpin,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessInsuranceAndIsActiveAndIsDeleted(int precedence,boolean processInsurance,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessCableTvAndIsActiveAndIsDeleted(int precedence,boolean processCableTv,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessAirlineTicketAndIsActiveAndIsDeleted(int precedence,boolean processAirlineTicket,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessElectricityAndIsActiveAndIsDeleted(int precedence,boolean processElectricity,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessAirtimeAndIsActiveAndIsDeleted(int precedence,boolean processAirtime,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessDataBundleAndIsActiveAndIsDeleted(int precedence,boolean processDataBundle,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessBettingAndIsActiveAndIsDeleted(int precedence,boolean processBetting,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessGovernmentPaymentAndIsActiveAndIsDeleted(int precedence,boolean processGovernmentPayment,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessSchoolFeesAndIsActiveAndIsDeleted(int precedence,boolean processSchoolFees,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessVisaFeesAndIsActiveAndIsDeleted(int precedence,boolean processVisaFees,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessTaxesLeviesAndIsActiveAndIsDeleted(int precedence,boolean processTaxesLevies,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessInternetSubscriptionAndIsActiveAndIsDeleted(int precedence,boolean processInternetSubscription,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessTithesDonationAndIsActiveAndIsDeleted(int precedence,boolean processTithesDonation,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessEducationAndIsActiveAndIsDeleted(int precedence,boolean processEducation,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessVehicleAndIsActiveAndIsDeleted(int precedence,boolean processVehicle,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessTransportAndIsActiveAndIsDeleted(int precedence,boolean processTransport,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessEmbassyAndIsActiveAndIsDeleted(int precedence,boolean processEmbassy,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessSchoolBoardAndIsActiveAndIsDeleted(int precedence,boolean processSchoolBoard,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessShoppingAndIsActiveAndIsDeleted(int precedence,boolean processShopping,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessEventTicketAndIsActiveAndIsDeleted(int precedence,boolean processEventTicket,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessOnlineShoppingAndIsActiveAndIsDeleted(int precedence,boolean processOnlineShopping,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessInsuranceInvestmentAndIsActiveAndIsDeleted(int precedence,boolean processInsuranceInvestment,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessInternationalAirtimeAndIsActiveAndIsDeleted(int precedence,boolean processInternationalAirtime,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessLagosStateCBSAndIsActiveAndIsDeleted(int precedence,boolean processLagosStateCBS,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessCreditLoanRepaymentAndIsActiveAndIsDeleted(int precedence,boolean processCreditLoanRepayment,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessPayTvSubscriptionAndIsActiveAndIsDeleted(int precedence,boolean processPayTvSubscription,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessReligiousInstitutionsAndIsActiveAndIsDeleted(int precedence,boolean processReligiousInstitutions,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessNestleDistributorsAndIsActiveAndIsDeleted(int precedence,boolean processNestleDistributors,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessBlackFridayAndIsActiveAndIsDeleted(int precedence,boolean processBlackFriday,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessApmTerminalsAndIsActiveAndIsDeleted(int precedence,boolean processApmTerminals,boolean isActive, boolean isDeleted);
    Optional<ServiceProvider> findFirstByPrecedenceAndProcessDealerPaymentsAndIsActiveAndIsDeleted(int precedence,boolean processDealerPayments,boolean isActive, boolean isDeleted);


    List<ServiceProvider> findAllByIsDeleted(boolean isDeleted);

    Optional<ServiceProvider> findFirstByNameStartingWithIgnoreCase(String name);
}
