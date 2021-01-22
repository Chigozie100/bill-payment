package com.wayapay.thirdpartyintegrationservice.service.interswitch;

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
public class GetAllBillersResponse {
    private List<BillerDetail> billers = new ArrayList<>();
}
