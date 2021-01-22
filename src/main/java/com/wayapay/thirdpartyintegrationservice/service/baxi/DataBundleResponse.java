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
public class DataBundleResponse extends SuperResponse {
    private List<DataBundle> data = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class DataBundle {
    private String name;
    private String allowance;
    private String price;
    private String validity;
    private String datacode;
}
