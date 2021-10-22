package com.wayapay.thirdpartyintegrationservice.service.interswitch;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QuickTellerUserParam {

//    private String customerId1;
//    private String customerId2;
    private String paymentCode;
    private String customerId;
    private String customerEmail;
    private String customerMobile;

}
