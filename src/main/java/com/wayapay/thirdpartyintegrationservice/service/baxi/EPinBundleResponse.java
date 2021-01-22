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
public class EPinBundleResponse extends SuperResponse{
    private List<EPinBundle> data = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class EPinBundle{
    private String amount;
    private String available;
    private String description;
}
