package com.wayapay.thirdpartyintegrationservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubItem {
    private String id;
    private String name;
    private String minAmount = "0";
    private String amount;



    public SubItem(String name) {
        this.id = name;
        this.name = name;
    }

    public SubItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

}