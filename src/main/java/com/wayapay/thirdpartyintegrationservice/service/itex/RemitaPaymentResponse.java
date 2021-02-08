package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RemitaPaymentResponse extends SuperResponse {
    private RemitaPaymentDetails data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class RemitaPaymentDetails{
    private Boolean error;
    private String responseCode;
    private String message;
    private String appVersionCode;
    private List<RemitaPaymentDetail> data;
    private String reference;
    private String transactionId;
    private String sequence;
    private String clientReference;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class RemitaPaymentDetail {
    private String rrr;
    private String totalAmount;
    private String balanceDue;
    private String paymentRef;
    private String paymentDate;
    private String debittedAccount;
    private String amountDebitted;
}