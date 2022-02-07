package com.wayapay.thirdpartyintegrationservice.dto;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class OtherDetailsDto {
    private String id;
    private String organisationName;
    private String organisationType;
    private String businessType;
    private String organizationCity;
    private String organizationAddress;
    private String organizationState;
    private String organisationEmail;
    private String organisationPhone;
    private String frontImage;
    private String leftImage;
    private String rightImage;

    @NotBlank(message = "please enter the other details id")
    private UUID otherDetailsId;
}
