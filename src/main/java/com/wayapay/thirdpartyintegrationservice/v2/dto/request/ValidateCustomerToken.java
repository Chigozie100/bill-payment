package com.wayapay.thirdpartyintegrationservice.v2.dto.request;

import lombok.Data;

@Data
public class ValidateCustomerToken {
    private String type;
    private String account;
    private Long serviceProviderId;
    private Long serviceProviderCategoryId;
}
