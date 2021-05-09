package com.wayapay.thirdpartyintegrationservice.dto;

import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryManagementResponse {

    private Long id;
    private String name;
    private String categoryAggregatorCode;
    private String categoryWayaPayCode;
    private boolean active;
    private Long aggregatorId;
    private ThirdPartyNames aggregatorName;

}
