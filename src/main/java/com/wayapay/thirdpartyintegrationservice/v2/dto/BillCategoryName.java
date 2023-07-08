package com.wayapay.thirdpartyintegrationservice.v2.dto;

import java.util.Optional;

public enum BillCategoryName {
    airtime , databundle , cabletv , epin ,
    betting ,electricity,education,vehicle,
    insurance,donation,airline, transport,
    tax,embassy,subscription,schoolboard,
    shopping,event_ticket,online_shopping,
    government_payments,insurance_and_investment,
    international_airtime, lagos_state_cbs,credit_and_loan_repayment,
    pay_tv_subscription,religious_institutions,nestle_distributors,
    black_friday,apm_terminals,dealer_payments;

    public static Optional<BillCategoryName> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(BillCategoryName.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static boolean isNonEmpty(String value){
        return value != null && !value.isEmpty();
    }
}
