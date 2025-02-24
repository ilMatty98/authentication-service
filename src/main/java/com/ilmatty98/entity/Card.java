package com.ilmatty98.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;


@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class Card extends BaseCredential {

    @Column(name = "card_holder", length = 100)
    private String cardHolder;

    @Column(name = "number", length = 100)
    private String number;

    @Column(name = "expiration")
    private LocalDate expiration;

    @Column(name = "cvv", length = 3)
    private String cvv;
}
