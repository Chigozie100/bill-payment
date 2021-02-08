package com.wayapay.thirdpartyintegrationservice.service.baxi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class EPinPaymentResponse extends SuperResponse {
    private EpinDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class EpinDetail extends PaymentDetail {
    private List<PinDetail> pins;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class PinDetail {
    private String instructions;
    private String serialNumber;
    private String pin;
    private String expiresOn;
}