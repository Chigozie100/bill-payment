package com.wayapay.thirdpartyintegrationservice.dto;

import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BillerManagementResponse {

    private Long id;
    private String name;
    private String billerAggregatorCode;
    private String billerWayaPayCode;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private ThirdPartyNames aggregatorName;

}
