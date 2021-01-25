package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class InternetValidationResponse extends SuperResponse {
    private InternetValidationReport data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class InternetValidationReport {
    private String error;
    private String message;
    private String customerName;
    private String responseCode;
    private String description;
    private String productCode;
}

