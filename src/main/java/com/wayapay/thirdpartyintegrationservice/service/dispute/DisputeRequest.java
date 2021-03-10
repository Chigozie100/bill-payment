package com.wayapay.thirdpartyintegrationservice.service.dispute;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
