package com.wayapay.thirdpartyintegrationservice.service.interswitch;

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
public class CategoryResponse {

    private List<CategoryDetail> categorys = new ArrayList();

}

@Getter
@Setter
@NoArgsConstructor
@ToString
class CategoryDetail{
    private String categoryid;
    private String categoryname;
    private String categorydescription;
}
