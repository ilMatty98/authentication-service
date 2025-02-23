package com.ilmatty98.service;

import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.constants.EmailTypeEnum;
import com.ilmatty98.constants.TokenClaimEnum;
import com.ilmatty98.dto.request.*;
import com.ilmatty98.dto.response.AccessDto;
import com.ilmatty98.entity.Account;
import com.ilmatty98.mapper.AuthenticationMapper;
import com.ilmatty98.repository.AccountRepository;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
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

    private final AccountRepository accountRepository;

    private final AuthenticationMapper authenticationMapper;

    @Transactional
    public boolean signUp(SignUpDto signUpDto) {
        log.info("Init signUp for account {}", signUpDto.getEmail());
        if (accountRepository.existsByEmail(signUpDto.getEmail())) {
            log.warn("Account {} already registered", signUpDto.getEmail());
            throw new BadRequestException();
        }

        var salt = AuthenticationUtils.generateSalt(saltSize);
        var hash = AuthenticationUtils.generateArgon2id(signUpDto.getMasterPasswordHash(), salt, argon2idSize,
                argon2idIterations, argon2idMemoryKB, argon2idParallelism);

        var account = authenticationMapper.newAccount(signUpDto, salt, hash, getCurrentTimestamp(), AccountStateEnum.UNVERIFIED);

        var dynamicLabels = Collections.singletonMap("href", endpointFe + "/" + account.getEmail() + "/" + account.getVerificationCode() + "/confirm");

        emailService.sendEmail(account.getEmail(), account.getLanguage(), EmailTypeEnum.SING_UP, dynamicLabels, true);
        accountRepository.persist(account);
        log.info("End signUp for account {}", signUpDto.getEmail());
        return true;
    }

    @Transactional
    public AccessDto logIn(LogInDto logInDto) {
        log.info("Init logIn for account {}", logInDto.getEmail());
        var account = getAccount(() -> accountRepository.findByEmail(logInDto.getEmail()), logInDto.getEmail());

        if (AccountStateEnum.UNVERIFIED.equals(account.getState())) {
            log.warn("Account {} not confirmed", logInDto.getEmail());
            throw new NotAuthorizedException("");
        }

        checkPassword(account, logInDto.getMasterPasswordHash());

        account.setTimestampLastAccess(getCurrentTimestamp());
        accountRepository.persist(account);

        var claims = new HashMap<String, Object>();
        claims.put(TokenClaimEnum.ID.getLabel(), account.getId());
        claims.put(TokenClaimEnum.EMAIL.getLabel(), account.getEmail());
        claims.put(TokenClaimEnum.ROLE.getLabel(), account.getState().name());

        var token = tokenJwtService.generateTokenJwt(claims);

        var dynamicLabels = Map.ofEntries(
                entry("date_value", logInDto.getLocalDateTime()),
                entry("ipAddress_value", logInDto.getIpAddress()),
                entry("device_value", logInDto.getDeviceType())
        );

        emailService.sendEmail(account.getEmail(), account.getLanguage(), EmailTypeEnum.LOG_IN, dynamicLabels, false);
        log.info("End logIn for account {}", logInDto.getEmail());
        return authenticationMapper.newAccessDto(account, token, tokenJwtService.getPublicKey());
    }

    public boolean checkEmail(String email) {
        log.info("Init checkEmail for account {}", email);
        return accountRepository.existsByEmail(email);
    }

    @Transactional
    public boolean confirmEmail(String email, String code) {
        log.info("Init confirmEmail for account {}", email);
        var account = getAccount(() -> accountRepository.findByEmailAndVerificationCode(email, code), email);

        account.setState(AccountStateEnum.VERIFIED);
        account.setVerificationCode(null);
        accountRepository.persist(account);
        log.info("End confirmEmail for account {}", email);
        return true;
    }

    @Transactional
    public boolean changePassword(ChangePasswordDto changePasswordDto, String email) {
        log.info("Init changePassword for account {}", email);
        var account = getAccount(() -> accountRepository.findByEmailAndState(email, AccountStateEnum.VERIFIED), email);

        checkPassword(account, changePasswordDto.getCurrentMasterPasswordHash());

        var salt = AuthenticationUtils.generateSalt(saltSize);
        var hash = AuthenticationUtils.generateArgon2id(changePasswordDto.getNewMasterPasswordHash(), salt, argon2idSize,
                argon2idIterations, argon2idMemoryKB, argon2idParallelism);

        account.setTimestampPassword(getCurrentTimestamp());
        account.setSalt(authenticationMapper.base64Encoding(salt));
        account.setHash(authenticationMapper.base64Encoding(hash));
        account.setInitializationVector(authenticationMapper.base64EncodingString(changePasswordDto.getNewInitializationVector()));
        account.setProtectedSymmetricKey(authenticationMapper.base64EncodingString(changePasswordDto.getNewProtectedSymmetricKey()));

        emailService.sendEmail(account.getEmail(), account.getLanguage(), EmailTypeEnum.CHANGE_PSW, new HashMap<>(), true);
        accountRepository.persist(account);
        log.info("End changePassword for account {}", email);
        return true;
    }

    public boolean sendHint(String email) {
        log.info("Init sendHint for account {}", email);
        var account = getAccount(() -> accountRepository.findByEmailAndState(email, AccountStateEnum.VERIFIED), email);

        var dynamicLabels = Map.ofEntries(entry("hint_value", account.getHint()));
        emailService.sendEmail(account.getEmail(), account.getLanguage(), EmailTypeEnum.SEND_HINT, dynamicLabels, true);
        log.info("End sendHint for account {}", email);
        return true;
    }

    @Transactional
    public boolean deleteAccount(String email, DeleteDto deleteDto) {
        log.info("Init deleteAccount for account {}", email);
        var account = getAccount(() -> accountRepository.findByEmailAndState(email, AccountStateEnum.VERIFIED), email);

        checkPassword(account, deleteDto.getMasterPasswordHash());

        accountRepository.delete(account);
        emailService.sendEmail(account.getEmail(), account.getLanguage(), EmailTypeEnum.DELETE_ACCOUNT, new HashMap<>(), true);
        log.info("End deleteAccount for account {}", email);
        return true;
    }

    @Transactional
    public boolean changeEmail(ChangeEmailDto changeEmailDto, String oldEmail) {
        log.info("Init changeEmail for account {} to {}", oldEmail, changeEmailDto.getEmail());
        if (oldEmail.equals(changeEmailDto.getEmail()) || accountRepository.existsByEmail(changeEmailDto.getEmail())) {
            log.warn("Email {} already registered", changeEmailDto.getEmail());
            throw new BadRequestException();
        }

        var account = getAccount(() -> accountRepository.findByEmailAndState(oldEmail, AccountStateEnum.VERIFIED), oldEmail);

        checkPassword(account, changeEmailDto.getMasterPasswordHash());

        account.setTimestampEmail(getCurrentTimestamp());
        account.setVerificationCode(generateVerificationCode());
        account.setNewEmail(changeEmailDto.getEmail());
        account.setAttempt(0);

        var dynamicLabels = Map.ofEntries(entry("email", changeEmailDto.getEmail()));
        emailService.sendEmail(oldEmail, account.getLanguage(), EmailTypeEnum.CHANGE_EMAIL_NOTIFICATION, dynamicLabels, true);

        dynamicLabels = Map.ofEntries(entry("code", account.getVerificationCode()));
        emailService.sendEmail(changeEmailDto.getEmail(), account.getLanguage(), EmailTypeEnum.CHANGE_EMAIL_CODE, dynamicLabels, true);
        accountRepository.persist(account);
        log.info("End changeEmail for account {} to {}", oldEmail, changeEmailDto.getEmail());
        return true;
    }

    @Transactional(dontRollbackOn = BadRequestException.class)
    public boolean confirmChangeEmail(ConfirmChangeEmailDto confirmChangeEmailDto, String oldEmail) {
        log.info("Init confirmChangeEmail for account {} to {}", oldEmail, confirmChangeEmailDto.getEmail());
        if (accountRepository.existsByEmail(confirmChangeEmailDto.getEmail())) {
            log.warn("Email {} already registered", confirmChangeEmailDto.getEmail());
            throw new BadRequestException();
        }

        var account = getAccount(() -> accountRepository.findByEmailAndNewEmailAndState(oldEmail, confirmChangeEmailDto.getEmail(), AccountStateEnum.VERIFIED), oldEmail);

        checkPassword(account, confirmChangeEmailDto.getMasterPasswordHash());

        String errorMessage = null;
        var currentTime = LocalDateTime.now();
        var maximumTime = account.getTimestampEmail().toLocalDateTime().plusMinutes(emailChangeExpirationMn);

        if (currentTime.isAfter(maximumTime)) { // Time out
            errorMessage = String.format("The maximum time limit has been exceeded for account %s to %s",
                    oldEmail, confirmChangeEmailDto.getEmail());
        } else if (account.getAttempt() >= emailChangeAttempts) { // The attempt limit has been reached
            errorMessage = String.format("The attempt limit has been reached for account %s to %s",
                    oldEmail, confirmChangeEmailDto.getEmail());
        } else if (!confirmChangeEmailDto.getVerificationCode().equals(account.getVerificationCode())) { //Incorrect verification code
            account.setAttempt(account.getAttempt() + 1);
            accountRepository.persist(account);
            log.warn("Incorrect verification code for account {} to {}", oldEmail, confirmChangeEmailDto.getEmail());
            throw new BadRequestException();
        } else { // Ok
            var salt = AuthenticationUtils.generateSalt(saltSize);
            var hash = AuthenticationUtils.generateArgon2id(confirmChangeEmailDto.getNewMasterPasswordHash(), salt,
                    argon2idSize, argon2idIterations, argon2idMemoryKB, argon2idParallelism);

            account.setEmail(account.getNewEmail());
            account.setSalt(authenticationMapper.base64Encoding(salt));
            account.setHash(authenticationMapper.base64Encoding(hash));
            account.setInitializationVector(authenticationMapper.base64EncodingString(confirmChangeEmailDto.getNewInitializationVector()));
            account.setProtectedSymmetricKey(authenticationMapper.base64EncodingString(confirmChangeEmailDto.getNewProtectedSymmetricKey()));
            emailService.sendEmail(account.getEmail(), account.getLanguage(), EmailTypeEnum.CHANGE_EMAIL, new HashMap<>(), true);
        }

        account.setVerificationCode(null);
        account.setNewEmail(null);
        account.setAttempt(null);
        accountRepository.persist(account);

        if (errorMessage != null) {
            log.warn(errorMessage);
            throw new BadRequestException();
        }

        log.info("End confirmChangeEmail for account {} to {}", oldEmail, confirmChangeEmailDto.getEmail());
        return true;
    }

    @Transactional
    public boolean changeInformation(ChangeInformationDto changeInformationDto, String email) {
        log.info("Init changeInformation for account {}", email);
        var account = getAccount(() -> accountRepository.findByEmailAndState(email, AccountStateEnum.VERIFIED), email);

        account.setHint(changeInformationDto.getHint());
        account.setLanguage(changeInformationDto.getLanguage());
        account.setPropic(changeInformationDto.getPropic());
        accountRepository.persist(account);

        log.info("End changeInformation for account {}", email);
        return true;
    }

    private Account getAccount(Supplier<Optional<Account>> accountSupplier, String email) {
        return accountSupplier.get().orElseThrow(() -> {
            log.warn("Account {} not found", email);
            return new NotFoundException();
        });
    }

    private static Timestamp getCurrentTimestamp() {
        return Timestamp.from(Instant.now());
    }

    private void checkPassword(Account account, String masterPasswordHash) {
        var storedHash = Base64.getDecoder().decode(account.getHash());
        var salt = Base64.getDecoder().decode(account.getSalt());
        var currentHash = AuthenticationUtils.generateArgon2id(masterPasswordHash, salt,
                argon2idSize, argon2idIterations, argon2idMemoryKB, argon2idParallelism);

        if (!Arrays.equals(storedHash, currentHash)) {
            log.warn("Invalid credentials for account {}", account.getEmail());
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
