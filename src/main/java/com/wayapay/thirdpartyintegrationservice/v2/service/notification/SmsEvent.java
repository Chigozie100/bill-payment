package com.wayapay.thirdpartyintegrationservice.v2.service.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SmsEvent {

    @NotNull(message = "make sure you entered the right key *initiator* , and the value must not be null")
    @NotBlank(message = "initiator field must not be blank, and make sure you use the right key *initiator*")
    private String initiator;

    @NotNull(message = "make sure you entered the right key *eventType* , and the value must not be null")
    @Pattern(regexp = "(SMS)", message = "must match 'SMS'")
    private String eventType;

    @JsonIgnore
    private String key;

    @Valid
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private SmsPayload data;

    @ApiModelProperty(example = "This is to be used by Billspayment only")
    private PaymentTransactionDetailDto paymentTransactionDetail;

    public SmsEvent(SmsPayload data, String eventType) {
        this.data = data;
        this.eventType = eventType;
    }

    public SmsEvent(String eventType, SmsPayload data, PaymentTransactionDetailDto paymentTransactionDetail) {
        this.eventType = eventType;
        this.data = data;
        this.paymentTransactionDetail = paymentTransactionDetail;
    }
}