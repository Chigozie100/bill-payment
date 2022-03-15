package com.wayapay.thirdpartyintegrationservice.service.notification;

import com.wayapay.thirdpartyintegrationservice.util.EventCategory;
import com.wayapay.thirdpartyintegrationservice.util.ProductType;
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
public class EmailEvent {

    @NotNull(message = "make sure you entered the right key *eventType* , and the value must not be null")
    @Pattern(regexp = "(EMAIL)", message = "must match 'EMAIL'")
    private String eventType;

    private EventCategory eventCategory;

    private ProductType productType;

    @NotNull(message = "make sure you entered the right key *initiator* , and the value must not be null")
    @NotBlank(message = "initiator field must not be blank, and make sure you use the right key *initiator*")
    private String initiator;
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private String transactionId;
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private String amount;
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private String transactionDate;

    private String narration;

    @Valid
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private EmailPayload data;

    public EmailEvent(EmailPayload data, String eventType) {
        this.data = data;
        this.eventType = eventType;
    }
}