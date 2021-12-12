package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.Data;

@Data
public class WalletTransactionPojo {
    private Long id;
    private boolean del_flg;
    private boolean posted_flg;
    private String tranId;
    private String acctNum;
    private Double tranAmount;
    private String tranType;
    private String partTranType;
    private String tranNarrate;
    private String tranDate;
    private String tranCrncyCode;
    private String paymentReference;
    private String tranGL;
    private Long tranPart;

}

