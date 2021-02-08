package com.wayapay.thirdpartyintegrationservice.service.itex;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MultiChoiceBouquetResponse extends SuperResponse {
    private MultiChoiceData data;
}

@Getter
@Setter
@NoArgsConstructor
@ToString
class MultiChoiceData {
    private String responseCode;
    private BouquetDetail bouquets;
}
