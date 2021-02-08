package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ElectricPaymentResponse extends SuperResponse {
    private ElectricDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class ElectricDetail {
    private String transactionStatus;
    private String transactionReference;
    private String statusCode;
    private String transactionMessage;
    private String tokenCode;
    private String tokenAmount;
    private String amountOfPower;
    private String creditToken;
    private String resetToken;
    private String configureToken;
}
