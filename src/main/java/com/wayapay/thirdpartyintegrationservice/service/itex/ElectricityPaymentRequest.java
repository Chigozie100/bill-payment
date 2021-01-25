package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.util.Strings;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ElectricityPaymentRequest {
    private String customerPhoneNumber;
    private String paymentMethod;
    private String service;
    private String clientReference;
    private String pin;
    private String productCode;
    private Object card = Strings.EMPTY;

}
