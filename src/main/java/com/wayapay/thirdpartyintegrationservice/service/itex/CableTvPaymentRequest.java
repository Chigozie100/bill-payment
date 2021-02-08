package com.wayapay.thirdpartyintegrationservice.service.itex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.util.Strings;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CableTvPaymentRequest {
    private String phone;
    private String bouquet;
    private String cycle;
    private String code;
    private String paymentMethod;
    private String service;
    private String clientReference;
    private String pin;
    private String productCode;
    private Object card = Strings.EMPTY;
}
