package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DataValidationResponse extends SuperResponse {
    private DataValidationReport data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class DataValidationReport {
    private String status;
    private String date;
    private String responseCode;
    private String productCode;
    private List<Plan> data = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class Plan {
    private String type;
    private String tariff_id;
    private String code;
    private String duration;
    private String amount;
    private String value;
    private String description;
}
