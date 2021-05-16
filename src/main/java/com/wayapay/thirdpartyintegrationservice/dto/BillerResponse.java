package com.wayapay.thirdpartyintegrationservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BillerResponse {

    @ApiModelProperty(notes = "Biller Id", example = "mtnvtu")
    private String billerId;

    @ApiModelProperty(notes = "Biller Name", example = "MTN VTU")
    private String billerName;

    @ApiModelProperty(notes = "Biller WayaPay Name", example = "WayaPayAirtime")
    private String billerWayaPayName;

    @ApiModelProperty(notes = "Category Id", example = "Airtime")
    private String categoryId;

    public BillerResponse(String billerId, String billerName, String categoryId) {
        this.billerId = billerId;
        this.billerName = billerName;
        this.categoryId = categoryId;
    }
}
