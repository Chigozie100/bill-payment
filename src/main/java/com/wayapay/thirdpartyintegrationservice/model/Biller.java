package com.wayapay.thirdpartyintegrationservice.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "Biller", uniqueConstraints = {@UniqueConstraint(columnNames = {"biller_aggregator_code", "category_id"})})
public class Biller extends SuperModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "biller_aggregator_code", nullable = false)
    private String billerAggregatorCode;

    @Column(name = "biller_wayapay_Code")
    private String billerWayaPayCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    private boolean active = true;

    public Biller(String name, String billerAggregatorCode, Category category) {
        this.name = name;
        this.billerAggregatorCode = billerAggregatorCode;
        this.category = category;
    }
}
