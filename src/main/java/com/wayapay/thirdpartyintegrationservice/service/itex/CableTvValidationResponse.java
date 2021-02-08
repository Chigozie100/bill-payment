package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CableTvValidationResponse extends SuperResponse {
    private CableTvValidationDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class CableTvValidationDetail{
    private Integer status;
    private Boolean error;
    private String ref;
    private String name;
    private String unit;
    private String date;
    private String smartCardCode;
    private String balance;
    private String bouquetCode;
    private String bouquet;
    private String serviceId;
    private String customerNumber;
    private String responseCode;
    private String message;
    private String description;
    private String account;
    private String type;
    private String basketId;
    private List<BouquetDetail> bouquets;
    private String productCode;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class BouquetDetail {
    private String name;
    private String product_code;
    private String amount;
    private String description;
    private String code;
    private Cycle cycles;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class Cycle {
    private String daily;
    private String weekly;
    private String monthly;
}

