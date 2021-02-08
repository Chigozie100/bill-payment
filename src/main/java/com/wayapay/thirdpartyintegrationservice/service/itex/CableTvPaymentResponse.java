package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CableTvPaymentResponse extends SuperResponse {
    private CableTvResponseData data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class CableTvResponseData {
    private Boolean error;
    private Boolean smartCardCode;
    private Boolean name;
    private String message;
    private String description;
    private String ref;
    private String amount;
    private String reversal;
    private String bouquet;
    private String bouquetCode;
    private String bouquetName;
    private String account;
    private String externalReference;
    private String auditReferenceNumber;
    private String date;
    private MultiChoiceResponseSubData response;
    private String responseCode;
    private String reference;
    private String sequence;
    private String clientReference;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class MultiChoiceResponseSubData {
    private String customerCareReferenceId;
    private String exchangeReference;
    private String errorMessage;
    private String errorCode;
    private String errorId;
    private String auditReferenceNumber;
    private String done;
    private String status;
}
