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

    private Long id;
    private String transactionId;
    private ThirdPartyNames thirdPartyName;
    private BigDecimal amount;
    private Boolean successful;
    private String category;
    private String biller;
    private String paymentRequest;
    private String paymentResponse;
    private String referralCode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+1")
    private Date transactionDateTime;
    private String username;
    private String email;
    private String userAccountNumber;

    public TransactionDetail(Long id,String transactionId, ThirdPartyNames thirdPartyName, BigDecimal amount, Boolean successful,
                             String category, String biller, String paymentRequest, String paymentResponse, Date transactionDateTime) {
        this.id = id;
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

    public TransactionDetail(Long id, String transactionId, ThirdPartyNames thirdPartyName, BigDecimal amount, Boolean successful,
                             String category, String biller, String referralCode,String paymentRequest, String paymentResponse, Date transactionDateTime, String username, String email, String userAccountNumber) {
        this.id = id;
        this.transactionId = transactionId;
        this.thirdPartyName = thirdPartyName;
        this.amount = amount;
        this.successful = successful;
        this.category = category;
        this.biller = biller;
        this.referralCode = referralCode;
        this.paymentRequest = paymentRequest;
        this.paymentResponse = paymentResponse;
        this.transactionDateTime = transactionDateTime;
        this.username = username;
        this.email = email;
        this.userAccountNumber = userAccountNumber;
    }

    public TransactionDetail(String username, String referralCode) {
        this.username = username;
        this.referralCode = referralCode;
    }
}
