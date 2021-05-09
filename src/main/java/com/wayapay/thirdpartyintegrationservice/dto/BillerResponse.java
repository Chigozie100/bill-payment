package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BillerResponse {

    private String billerId;
    private String billerName;
    private String billerWayaPayName;
    private String categoryId;

    public BillerResponse(String billerId, String billerName, String categoryId) {
        this.billerId = billerId;
        this.billerName = billerName;
        this.categoryId = categoryId;
    }
}
