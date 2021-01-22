package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BillerResponse {

    private String billerId;
    private String billerName;
    private String categoryId;

}
