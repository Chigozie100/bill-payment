package com.wayapay.thirdpartyintegrationservice.service.notification;


import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmailRecipient {

    @NotBlank(message = "value must not be blank, also enter the right key *fullName*")
    private String fullName;

    @NotBlank(message = "value must not be blank, also enter the right key *email*")
    private String email;
}
