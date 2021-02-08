package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class LCCValidationResponse extends SuperResponse {
    private LCCDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class LCCDetail{
    private Integer status;
    private String merchantReference;
    private String custReference;
    private String customerReferenceAlternate;
    private String customerReferenceDescription;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String amount;
    private String productCode;
    private String quantity;
    private String price;
    private String subtotal;
    private String tax;
    private String total;
    private String error;
    private String message;
    private String responseCode;
}