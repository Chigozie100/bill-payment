package com.wayapay.thirdpartyintegrationservice.service.notification;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class EventBase {
    @NotNull(message = "make sure you entered the right key *initiator* , and the value must not be null")
    @NotBlank(message = "initiator field must not be blank, and make sure you use the right key *initiator*")
    private String initiator;
}