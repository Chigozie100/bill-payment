package com.wayapay.thirdpartyintegrationservice.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class WalletTxnResponse {
    private boolean status;
    private Integer code;
    private String message;
    private List<WalletTransData> data;
}
