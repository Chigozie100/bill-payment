package com.wayapay.thirdpartyintegrationservice.service.itex;

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
public class InternetBundleResponse extends SuperResponse{
    private InternetBundleDetail data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class InternetBundleDetail{
    private String error;
    private String message;
    private String responseCode;
    private String description;
    private List<Bundles> bundles = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class Bundles {
    private String name;
    private String code;
    private String displayPrice;
    private String price;
    private String fee;
    private String validity;
}
