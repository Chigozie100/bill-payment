package com.wayapay.thirdpartyintegrationservice.v2.service.quickteller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentItem {
    private String categoryid;
    private String billerid;
    private Boolean isAmountFixed;
    private String paymentitemid;
    private String paymentitemname;
    private String amount;
    private String code;
    private String currencyCode;
    private String currencySymbol;
    private String itemCurrencySymbol;
    private String sortOrder;
    private String pictureId;
    private String paymentCode;
    private String itemFee;
    private String paydirectItemCode;
}
