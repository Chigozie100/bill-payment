package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class ReversalDto {
    private String tranCrncy;
    private Date tranDate = new Date();
    private String tranId;
}
