package com.wayapay.thirdpartyintegrationservice.service.dispute;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DisputeRequest {

    private String extraDetails;
    private String narrationOfDispute;
    private String transactionAmount;
    private String transactionFee;
    private String transactionDate;
    private String transactionId;
    private String userId;


}
