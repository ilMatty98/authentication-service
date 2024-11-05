package com.ilmatty98.service;

import com.ilmatty98.constants.EmailTypeEnum;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.SignUpDto;
import com.ilmatty98.mapper.AuthenticationMapper;
import com.ilmatty98.repository.UserRepository;
import com.ilmatty98.utils.AuthenticationUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class AuthenticationService {

    @ConfigProperty(name = "fe.endpoint")
    String endpointFe;

    @ConfigProperty(name = "encryption.salt.size")
    int saltSize;

    @ConfigProperty(name = "encryption.argon2id.size")
    int argon2idSize;

    @ConfigProperty(name = "encryption.argon2id.iterations")
    int argon2idIterations;

    @ConfigProperty(name = "encryption.argon2id.memoryKB")
    int argon2idMemoryKB;

    @ConfigProperty(name = "encryption.argon2id.parallelism")
    int argon2idParallelism;

    @ConfigProperty(name = "change-email.expiration-minutes")
    int emailChangeExpirationMn;

    @ConfigProperty(name = "change-email.attempts")
    int emailChangeAttempts;

    private final EmailService emailService;
//
//    private final TokenJwtService tokenJwtService;

    private final UserRepository userRepository;

    private final AuthenticationMapper authenticationMapper;

    @SneakyThrows
    @Transactional
    public void signUp(SignUpDto signUpDto) {
        log.info("Init signUp for user {}", signUpDto.getEmail());
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            log.warn("User %{} already registered", signUpDto.getEmail());
            throw new BadRequestException();
        }


        var salt = AuthenticationUtils.generateSalt(saltSize);
        var hash = AuthenticationUtils.generateArgon2id(signUpDto.getMasterPasswordHash(), salt, argon2idSize,
                argon2idIterations, argon2idMemoryKB, argon2idParallelism);

        var user = authenticationMapper.newUser(signUpDto, salt, hash, getCurrentTimestamp(), UserStateEnum.UNVERIFIED);

        var dynamicLabels = Collections.singletonMap("href", endpointFe + "/" + user.getEmail() + "/" + user.getVerificationCode() + "/confirm");

        emailService.sendEmail(user.getEmail(), user.getLanguage(), EmailTypeEnum.SING_UP, dynamicLabels);
        userRepository.persist(user);
        log.info("End signUp for user {}", signUpDto.getEmail());
    }

    private static Timestamp getCurrentTimestamp() {
        return Timestamp.from(Instant.now());
    }
}
