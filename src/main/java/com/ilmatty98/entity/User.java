package com.ilmatty98.entity;


import com.ilmatty98.constants.UserStateEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "salt", length = 500, nullable = false)
    private String salt;

    @Column(name = "hash", length = 500, nullable = false)
    private String hash;

    @Column(name = "protected_symmetric_key", length = 500, nullable = false)
    private String protectedSymmetricKey;

    @Column(name = "initialization_vector", length = 500, nullable = false)
    private String initializationVector;

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

    @Column(name = "propic", length = 500, nullable = false)
    private String propic;

    @Column(name = "language", length = 2, nullable = false)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 10, nullable = false)
    private UserStateEnum state;

    @Column(name = "verification_code", length = 36)
    private String verificationCode;

    @Column(name = "attempt")
    private Integer attempt;

    @Column(name = "new_email", length = 100, unique = true)
    private String newEmail;
}
