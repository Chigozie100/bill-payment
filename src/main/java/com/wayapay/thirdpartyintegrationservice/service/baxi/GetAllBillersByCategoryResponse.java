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
    private List<BillerDetail> data = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class BillerDetail{

    private String id;
    private String serviceId;
    private String serviceCode;
    private String serviceCategory;
    private String biller_id;
    private String serviceBiller;
    private String serviceType;
    private String serviceName;
    private String serviceDescription;
    private String serviceHandler;
    private String serviceProvider;
    private String serviceEnabled;
    private String serviceStatus;
    private String serviceLogo;
    private String deployed;
    private String b2b_deployed;
    private String created_at;
    private String updated_at;

}
