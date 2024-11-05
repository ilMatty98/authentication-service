package com.ilmatty98.repository;

import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public boolean existsByEmail(String email) {
        return find("email", email).firstResultOptional().isPresent();
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> findByEmailAndState(String email, UserStateEnum state) {
        return find("email = ?1 and state = ?2", email, state).firstResultOptional();
    }

    public Optional<User> findByEmailAndNewEmailAndState(String email, String newEmail, UserStateEnum state) {
        return find("email = ?1 and newEmail = ?2 and state = ?3", email, newEmail, state).firstResultOptional();
    }

    public Optional<User> findByEmailAndVerificationCode(String email, String code) {
        return find("email = ?1 and verificationCode = ?2", email, code).firstResultOptional();
    }
}