package com.wayapay.thirdpartyintegrationservice.service.dispute;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DisputeResponse {

    private String timestamp;
    private String message;
    private boolean status;
    private String data;

}
