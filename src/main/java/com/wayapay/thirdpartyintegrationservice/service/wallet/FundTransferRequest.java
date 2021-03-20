package com.wayapay.thirdpartyintegrationservice.service.wallet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class FundTransferRequest {

    private String accountNo;
    private BigDecimal amount;
    private String description;
    private Long id;
    private String transactionType;

}
