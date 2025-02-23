package com.ilmatty98.entity;


import com.ilmatty98.constants.AccountStateEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@ToString(exclude = {"logins", "cards"})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "salt", nullable = false, columnDefinition = "CLOB")
    private String salt;

    @Column(name = "hash", nullable = false, columnDefinition = "CLOB")
    private String hash;

    @Column(name = "timestamp_creation", nullable = false)
    private Timestamp timestampCreation;

    @Column(name = "timestamp_last_access", nullable = false)
    private Timestamp timestampLastAccess;

    @Column(name = "timestamp_password", nullable = false)
    private Timestamp timestampPassword;

    @Column(name = "timestamp_email", nullable = false)
    private Timestamp timestampEmail;

    @Column(name = "hint", length = 100, nullable = false)
    private String hint;

    @Column(name = "propic", nullable = false, columnDefinition = "CLOB")
    private String propic;

    @Column(name = "language", length = 2, nullable = false)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 10, nullable = false)
    private AccountStateEnum state;

    @Column(name = "verification_code", length = 36)
    private String verificationCode;

    @Column(name = "attempt")
    private Integer attempt;

    @Column(name = "new_email", length = 100, unique = true)
    private String newEmail;

    @OneToMany(mappedBy = "account")
    private List<Login> logins;

    @OneToMany(mappedBy = "account")
    private List<Card> cards;
}
