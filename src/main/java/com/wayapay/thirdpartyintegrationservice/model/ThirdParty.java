package com.wayapay.thirdpartyintegrationservice.model;

import com.wayapay.thirdpartyintegrationservice.util.ThirdPartyNames;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class ThirdParty extends SuperModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "third_party_name", nullable = false, unique = true)
    private ThirdPartyNames thirdPartyNames;

    private boolean active = true;

    public ThirdParty(ThirdPartyNames thirdPartyNames) {
        this.thirdPartyNames = thirdPartyNames;
    }

    public ThirdParty(Long id) {
        this.id = id;
    }
}
