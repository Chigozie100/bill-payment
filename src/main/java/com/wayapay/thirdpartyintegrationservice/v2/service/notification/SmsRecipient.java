package com.wayapay.thirdpartyintegrationservice.v2.service.notification;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SmsRecipient {

    @Email(message = "please enter a valid email")
    @NotBlank(message = "please enter a valid email*")
    private String email;

    @NotBlank(message = "value must not be blank, also enter the right key *telephone*")
    private String telephone;
}
