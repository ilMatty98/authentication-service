package com.ilmatty98.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@MappedSuperclass
@ToString(exclude = {"account"})
public class Vault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "timestamp_creation", nullable = false)
    private Timestamp timestampCreation;

    @Column(name = "timestamp_updated", nullable = false)
    private Timestamp timestampUpdated;
}
