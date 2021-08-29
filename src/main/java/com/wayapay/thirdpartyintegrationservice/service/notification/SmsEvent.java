package com.wayapay.thirdpartyintegrationservice.service.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    public SmsEvent(SmsPayload data, String eventType) {
        this.data = data;
        this.eventType = eventType;
    }
}