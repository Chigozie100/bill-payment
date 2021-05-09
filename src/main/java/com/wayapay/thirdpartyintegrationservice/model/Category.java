package com.wayapay.thirdpartyintegrationservice.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "category", uniqueConstraints = {@UniqueConstraint(columnNames = {"category_aggregator_code", "thirdparty_id"})})
public class Category extends SuperModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category_aggregator_code", nullable = false)
    private String categoryAggregatorCode;

    @Column(name = "category_wayapay_code")
    private String categoryWayaPayCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "thirdparty_id", referencedColumnName = "id")
    private ThirdParty thirdParty;

    private boolean active = true;

    public Category(String name, String categoryAggregatorCode, ThirdParty thirdParty) {
        this.name = name;
        this.categoryAggregatorCode = categoryAggregatorCode;
        this.thirdParty = thirdParty;
    }
}
