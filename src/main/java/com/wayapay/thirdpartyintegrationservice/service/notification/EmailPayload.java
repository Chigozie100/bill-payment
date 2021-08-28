package com.wayapay.thirdpartyintegrationservice.service.notification;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ToString
public class EmailPayload {

    @NotNull(message = "make sure you entered the right key *message* , and the value must not be null")
    @NotBlank(message = "message cannot be blank, and make sure you use the right key *message*")
    private String message;

    @Valid
    @NotNull(message = "make sure you entered the right key *names* , and the value must not be null")
    @NotEmpty(message = "names list cannot be empty. also make sure you use the right key *names*")
    private List<EmailRecipient> names;
}
