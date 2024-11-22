package com.ilmatty98.service;

import com.ilmatty98.constants.EmailTypeEnum;
import com.ilmatty98.constants.TokenClaimEnum;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.*;
import com.ilmatty98.dto.response.AccessDto;
import com.ilmatty98.entity.User;
import com.ilmatty98.mapper.AuthenticationMapper;
import com.ilmatty98.repository.UserRepository;
import com.ilmatty98.utils.AuthenticationUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            throw new NotAuthorizedException("");
        }

        checkPassword(user, logInDto.getMasterPasswordHash());

        user.setTimestampLastAccess(getCurrentTimestamp());
        userRepository.persist(user);

        var claims = new HashMap<String, Object>();
        claims.put(TokenClaimEnum.ID.getLabel(), user.getId());
        claims.put(TokenClaimEnum.EMAIL.getLabel(), user.getEmail());
        claims.put(TokenClaimEnum.ROLE.getLabel(), user.getState().name());

        var token = tokenJwtService.generateTokenJwt(claims);

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

    @Transactional
    public boolean changePassword(ChangePasswordDto changePasswordDto, String email) {
        log.info("Init changePassword for user {}", email);
        var user = userRepository.findByEmailAndState(email, UserStateEnum.VERIFIED)
                .orElseThrow(() -> {
                    log.warn("User {} not found", email);
                    return new NotFoundException();
                });

        checkPassword(user, changePasswordDto.getCurrentMasterPasswordHash());

        var salt = AuthenticationUtils.generateSalt(saltSize);
        var hash = AuthenticationUtils.generateArgon2id(changePasswordDto.getNewMasterPasswordHash(), salt, argon2idSize,
                argon2idIterations, argon2idMemoryKB, argon2idParallelism);

        user.setTimestampPassword(getCurrentTimestamp());
        user.setSalt(authenticationMapper.base64Encoding(salt));
        user.setHash(authenticationMapper.base64Encoding(hash));
        user.setInitializationVector(authenticationMapper.base64EncodingString(changePasswordDto.getNewInitializationVector()));
        user.setProtectedSymmetricKey(authenticationMapper.base64EncodingString(changePasswordDto.getNewProtectedSymmetricKey()));

        emailService.sendEmail(user.getEmail(), user.getLanguage(), EmailTypeEnum.CHANGE_PSW, new HashMap<>());
        userRepository.persist(user);
        log.info("End changePassword for user {}", email);
        return true;
    }

    public boolean sendHint(String email) {
        log.info("Init sendHint for user {}", email);
        var user = userRepository.findByEmailAndState(email, UserStateEnum.VERIFIED)
                .orElseThrow(() -> {
                    log.warn("User {} not found", email);
                    return new NotFoundException();
                });

        var dynamicLabels = Map.ofEntries(entry("hint_value", user.getHint()));
        emailService.sendEmail(user.getEmail(), user.getLanguage(), EmailTypeEnum.SEND_HINT, dynamicLabels);
        log.info("End sendHint for user {}", email);
        return true;
    }

    @Transactional
    public boolean deleteAccount(String email, DeleteDto deleteDto) {
        log.info("Init deleteAccount for user {}", email);
        var user = userRepository.findByEmailAndState(email, UserStateEnum.VERIFIED)
                .orElseThrow(() -> {
                    log.warn("User {} not found", email);
                    return new NotFoundException();
                });

        checkPassword(user, deleteDto.getMasterPasswordHash());

        userRepository.delete(user);
        emailService.sendEmail(user.getEmail(), user.getLanguage(), EmailTypeEnum.DELETE_USER, new HashMap<>());
        log.info("End deleteAccount for user {}", email);
        return true;
    }

    @Transactional
    public boolean changeEmail(ChangeEmailDto changeEmailDto, String oldEmail) {
        log.info("Init changeEmail for user {} to {}", oldEmail, changeEmailDto.getEmail());
        if (oldEmail.equals(changeEmailDto.getEmail()) || userRepository.existsByEmail(changeEmailDto.getEmail())) {
            log.warn("Email already registered");
            throw new BadRequestException();
        }

        var user = userRepository.findByEmailAndState(oldEmail, UserStateEnum.VERIFIED)
                .orElseThrow(() -> {
                    log.warn("User {} not found", oldEmail);
                    return new NotFoundException();
                });

        checkPassword(user, changeEmailDto.getMasterPasswordHash());

        user.setTimestampEmail(getCurrentTimestamp());
        user.setVerificationCode(generateVerificationCode());
        user.setNewEmail(changeEmailDto.getEmail());
        user.setAttempt(0);

        var dynamicLabels = Map.ofEntries(entry("email", changeEmailDto.getEmail()));
        emailService.sendEmail(oldEmail, user.getLanguage(), EmailTypeEnum.CHANGE_EMAIL_NOTIFICATION, dynamicLabels);

        dynamicLabels = Map.ofEntries(entry("code", user.getVerificationCode()));
        emailService.sendEmail(changeEmailDto.getEmail(), user.getLanguage(), EmailTypeEnum.CHANGE_EMAIL_CODE, dynamicLabels);
        userRepository.persist(user);
        log.info("End changeEmail for user {} to {}", oldEmail, changeEmailDto.getEmail());
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
            throw new NotAuthorizedException("");
        }
    }

    private static String generateVerificationCode() {
        var secureRandom = new SecureRandom();
        return IntStream.range(0, 6)
                .mapToObj(i -> String.valueOf(secureRandom.nextInt(10)))
                .collect(Collectors.joining());
    }

}
