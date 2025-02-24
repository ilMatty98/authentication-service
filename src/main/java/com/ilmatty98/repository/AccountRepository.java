package com.ilmatty98.repository;

import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.entity.Account;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {

    public boolean existsByEmail(String email) {
        return find("email", email).firstResultOptional().isPresent();
    }

    public Optional<Account> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<Account> findByEmailAndState(String email, AccountStateEnum state) {
        return find("email = ?1 and state = ?2", email, state).firstResultOptional();
    }

    public Optional<Account> findByEmailAndNewEmailAndState(String email, String newEmail, AccountStateEnum state) {
        return find("email = ?1 and newEmail = ?2 and state = ?3", email, newEmail, state).firstResultOptional();
    }

    public Optional<Account> findByEmailAndVerificationCode(String email, String code) {
        return find("email = ?1 and verificationCode = ?2", email, code).firstResultOptional();
    }
}