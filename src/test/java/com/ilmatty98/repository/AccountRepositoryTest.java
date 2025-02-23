package com.ilmatty98.repository;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.entity.Account;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static io.smallrye.common.constraint.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AccountRepositoryTest extends AuthenticationServiceTests {

    @Test
    void testExistsByEmail() {
        assertFalse(userRepository.existsByEmail(EMAIL));
        signUp(EMAIL, PASSWORD);
        assertTrue(userRepository.existsByEmail(EMAIL));
    }

    @Test
    void testFindByEmail() {
        assertFalse(userRepository.findByEmail(EMAIL).isPresent());
        var user = signUp(EMAIL, PASSWORD);
        checkUser((userRepository) -> userRepository.findByEmail(EMAIL), user);
    }

    @Test
    @Transactional
    void testFindByEmailAndState() {
        var user = signUp(EMAIL, PASSWORD);
        user.setState(UserStateEnum.UNVERIFIED);
        userRepository.persist(user);

        assertFalse(userRepository.findByEmailAndState(EMAIL, UserStateEnum.VERIFIED).isPresent());

        user.setState(UserStateEnum.VERIFIED);
        userRepository.persist(user);

        checkUser((userRepository) -> userRepository.findByEmailAndState(EMAIL, UserStateEnum.VERIFIED), user);
    }

    @Test
    @Transactional
    void findByEmailAndNewEmailAndState() {
        var newEmail = EMAIL + ".";
        var user = signUp(EMAIL, PASSWORD);
        user.setState(UserStateEnum.UNVERIFIED);
        user.setNewEmail(newEmail);
        userRepository.persist(user);

        assertFalse(userRepository.findByEmailAndNewEmailAndState(EMAIL, newEmail, UserStateEnum.VERIFIED).isPresent());

        user.setState(UserStateEnum.VERIFIED);
        userRepository.persist(user);

        checkUser((userRepository) -> userRepository.findByEmailAndNewEmailAndState(EMAIL, newEmail, UserStateEnum.VERIFIED), user);
    }

    @Test
    @Transactional
    void testFindByEmailAndVerificationCode() {
        var user = signUp(EMAIL, PASSWORD);
        user.setVerificationCode("code1");
        userRepository.persist(user);

        assertFalse(userRepository.findByEmailAndVerificationCode(EMAIL, "code").isPresent());

        user.setVerificationCode("code");
        userRepository.persist(user);

        checkUser((userRepository) -> userRepository.findByEmailAndVerificationCode(EMAIL, "code"), user);
    }

    private void checkUser(Function<UserRepository, Optional<Account>> userRepositoryFunction, Account expectedAccount) {
        userRepositoryFunction.apply(userRepository)
                .ifPresentOrElse(u -> {
                    assertEquals(expectedAccount.getId(), u.getId());
                    assertEquals(expectedAccount.getEmail(), u.getEmail());
                    assertEquals(expectedAccount.getSalt(), u.getSalt());
                    assertEquals(expectedAccount.getHash(), u.getHash());
                    assertEquals(expectedAccount.getProtectedSymmetricKey(), u.getProtectedSymmetricKey());
                    assertEquals(expectedAccount.getInitializationVector(), u.getInitializationVector());
                    testBetweenTimestamp(expectedAccount.getTimestampCreation(), u.getTimestampCreation());
                    testBetweenTimestamp(expectedAccount.getTimestampLastAccess(), u.getTimestampLastAccess());
                    testBetweenTimestamp(expectedAccount.getTimestampPassword(), u.getTimestampPassword());
                    assertEquals(expectedAccount.getLanguage(), u.getLanguage());
                    assertEquals(expectedAccount.getHint(), u.getHint());
                    assertEquals(expectedAccount.getPropic(), u.getPropic());
                    assertEquals(expectedAccount.getState(), u.getState());
                    assertEquals(expectedAccount.getVerificationCode(), u.getVerificationCode());
                }, Assertions::fail);
    }

}
