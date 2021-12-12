package com.wayapay.thirdpartyintegrationservice.service.notification;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InAppRecipient {

    @NotBlank(message = "value must not be blank, also enter the right key *userId*")
    private String userId;
}
