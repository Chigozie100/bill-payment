package com.wayapay.thirdpartyintegrationservice.v2.service.notification;

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
@NoArgsConstructor
@ToString
public class InAppEvent {
    @NotNull(message = "make sure you entered the right key *initiator* , and the value must not be null")
    @NotBlank(message = "initiator field must not be blank, and make sure you use the right key *initiator*")
    private String initiator;

    @NotNull(message = "make sure you entered the right key *eventType* , and the value must not be null")
    @Pattern(regexp = "(IN_APP)", message = "must match 'IN_APP'")
    private String eventType;

    private String category;
    private String token;

    @Valid
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private InAppPayload data;

    public InAppEvent(InAppPayload data, String eventType) {
        this.data = data;
        this.eventType = eventType;
    }
}
