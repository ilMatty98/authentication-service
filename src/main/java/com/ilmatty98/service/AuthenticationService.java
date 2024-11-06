package com.ilmatty98.service;

import com.ilmatty98.constants.EmailTypeEnum;
import com.ilmatty98.constants.TokenClaimEnum;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.LogInDto;
import com.ilmatty98.dto.request.SignUpDto;
import com.ilmatty98.dto.response.AccessDto;
import com.ilmatty98.entity.User;
import com.ilmatty98.mapper.AuthenticationMapper;
import com.ilmatty98.repository.UserRepository;
import com.ilmatty98.utils.AuthenticationUtils;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static java.util.Map.entry;

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

    private final TokenJwtService tokenJwtService;

    private final UserRepository userRepository;

    private final AuthenticationMapper authenticationMapper;

    @Transactional
    public boolean signUp(SignUpDto signUpDto) {
        log.info("Init signUp for user {}", signUpDto.getEmail());
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            log.warn("User {} already registered", signUpDto.getEmail());
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
        return true;
    }

    @Transactional
    public AccessDto logIn(LogInDto logInDto) {
        log.info("Init logIn for user {}", logInDto.getEmail());
        var user = userRepository.findByEmail(logInDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("User {} not found", logInDto.getEmail());
                    return new NotFoundException();
                });

        if (UserStateEnum.UNVERIFIED.equals(user.getState())) {
            log.warn("User {} not confirmed", logInDto.getEmail());
            throw new UnauthorizedException();
        }

        checkPassword(user, logInDto.getMasterPasswordHash());

        user.setTimestampLastAccess(getCurrentTimestamp());
        userRepository.persist(user);

        var claims = new HashMap<String, Object>();
        claims.put(TokenClaimEnum.ID.getLabel(), user.getId());
        claims.put(TokenClaimEnum.EMAIL.getLabel(), user.getEmail());
        claims.put(TokenClaimEnum.ROLE.getLabel(), user.getState().name());

        var token = tokenJwtService.generateTokenJwt(user.getEmail(), claims);

        var dynamicLabels = Map.ofEntries(
                entry("date_value", logInDto.getLocalDateTime()),
                entry("ipAddress_value", logInDto.getIpAddress()),
                entry("device_value", logInDto.getDeviceType())
        );

        emailService.sendEmail(user.getEmail(), user.getLanguage(), EmailTypeEnum.LOG_IN, dynamicLabels);
        log.info("End logIn for user {}", logInDto.getEmail());
        return authenticationMapper.newAccessDto(user, token, tokenJwtService.getPublicKey());
    }

    public boolean checkEmail(String email) {
        log.info("Init checkEmail for user {}", email);
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public boolean confirmEmail(String email, String code) {
        log.info("Init confirmEmail for user {}", email);
        var user = userRepository.findByEmailAndVerificationCode(email, code)
                .orElseThrow(() -> {
                    log.warn("User {} not found", email);
                    return new NotFoundException();
                });

        user.setState(UserStateEnum.VERIFIED);
        user.setVerificationCode(null);
        userRepository.persist(user);
        log.info("End confirmEmail for user {}", email);
        return true;
    }

    private static Timestamp getCurrentTimestamp() {
        return Timestamp.from(Instant.now());
    }

    private void checkPassword(User user, String masterPasswordHash) {
        var storedHash = Base64.getDecoder().decode(user.getHash());
        var salt = Base64.getDecoder().decode(user.getSalt());
        var currentHash = AuthenticationUtils.generateArgon2id(masterPasswordHash, salt,
                argon2idSize, argon2idIterations, argon2idMemoryKB, argon2idParallelism);

        if (!Arrays.equals(storedHash, currentHash)) {
            log.warn("Invalid credentials for user {}", user.getEmail());
            throw new UnauthorizedException();
        }
    }


}
