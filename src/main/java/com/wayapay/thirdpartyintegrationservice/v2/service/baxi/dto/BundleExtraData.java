package com.wayapay.thirdpartyintegrationservice.v2.service.baxi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class BundleExtraData {
    private String purchasedDuration;
    private String validUntil;
    private String purchasedPackage;
    private String voucherCode;
    private String captureUrl;
}
