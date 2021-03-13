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
    
    private BigDecimal amount;
    private String fromAccount;
    private String id;
    private String toAccount;
    
}
