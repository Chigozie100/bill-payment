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
public class CategoryResponse extends SuperResponse{
    private List<Category> data = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class Category {
    private String name;
    private String service_type;
}
