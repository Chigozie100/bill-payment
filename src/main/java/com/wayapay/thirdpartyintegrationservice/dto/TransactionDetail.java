package com.wayapay.thirdpartyintegrationservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TransactionDetail {

    private String transactionId;
    private ThirdPartyNames thirdPartyName;
    private BigDecimal amount;
    private Boolean successful;
    private String category;
    private String biller;
    private String paymentRequest;
    private String paymentResponse;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+1")
    private Date transactionDateTime;

    public TransactionDetail(String transactionId, ThirdPartyNames thirdPartyName, BigDecimal amount, Boolean successful,
                             String category, String biller, String paymentRequest, String paymentResponse, Date transactionDateTime) {
        this.transactionId = transactionId;
        this.thirdPartyName = thirdPartyName;
        this.amount = amount;
        this.successful = successful;
        this.category = category;
        this.biller = biller;
        this.paymentRequest = paymentRequest;
        this.paymentResponse = paymentResponse;
        this.transactionDateTime = transactionDateTime;
    }
}
