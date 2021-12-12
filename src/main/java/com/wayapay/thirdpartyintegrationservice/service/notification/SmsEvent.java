package com.wayapay.thirdpartyintegrationservice.service.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentResponse;
import com.wayapay.thirdpartyintegrationservice.dto.PaymentTransactionDetailDto;
import com.wayapay.thirdpartyintegrationservice.model.PaymentTransactionDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SmsEvent extends EventBase {


    @NotNull(message = "make sure you entered the right key *eventType* , and the value must not be null")
    @Pattern(regexp = "(SMS)", message = "must match 'SMS'")
    private String eventType;

    @JsonIgnore
    private String key;

    @Valid
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private SmsPayload data;


    @ApiModelProperty(example = "This is to be used by Billspayment only")
    private PaymentResponse paymentResponse;

    @ApiModelProperty(example = "This is to be used by Billspayment only")
    private PaymentTransactionDetailDto paymentTransactionDetail;

    public SmsEvent(SmsPayload data, String eventType) {
        this.data = data;
        this.eventType = eventType;
    }

    public SmsEvent(String eventType, SmsPayload data, PaymentResponse paymentResponse, PaymentTransactionDetailDto paymentTransactionDetail) {
        this.eventType = eventType;
        this.data = data;
        this.paymentResponse = paymentResponse;
        this.paymentTransactionDetail = paymentTransactionDetail;
    }
}