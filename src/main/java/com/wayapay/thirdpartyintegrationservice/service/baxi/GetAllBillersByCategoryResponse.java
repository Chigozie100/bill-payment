package com.wayapay.thirdpartyintegrationservice.service.baxi;

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
public class GetAllBillersByCategoryResponse extends SuperResponse {
    private Provider data;

}

@Getter
@Setter
@NoArgsConstructor
@ToString
class Provider {
    private List<BillerDetail> providers = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class BillerDetail{
    private String service_type;
    private String shortname;
    private String biller_id;
    private String product_id;
    private String name;
}
