package com.ilmatty98.repository;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.entity.User;
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
class UserRepositoryTest extends AuthenticationServiceTests {

    @Test
    void testExistsByEmail() throws Exception {
        assertFalse(userRepository.existsByEmail(EMAIL));
        signUp(EMAIL, PASSWORD);
        assertTrue(userRepository.existsByEmail(EMAIL));
    }

    @Test
    void testFindByEmail() throws Exception {
        assertFalse(userRepository.findByEmail(EMAIL).isPresent());
        var user = signUp(EMAIL, PASSWORD);
        checkUser((userRepository) -> userRepository.findByEmail(EMAIL), user);
    }

    @Test
    @Transactional
    void testFindByEmailAndState() throws Exception {
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
    void findByEmailAndNewEmailAndState() throws Exception {
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
    void testFindByEmailAndVerificationCode() throws Exception {
        var user = signUp(EMAIL, PASSWORD);
        user.setVerificationCode("code1");
        userRepository.persist(user);

        assertFalse(userRepository.findByEmailAndVerificationCode(EMAIL, "code").isPresent());

        user.setVerificationCode("code");
        userRepository.persist(user);

        checkUser((userRepository) -> userRepository.findByEmailAndVerificationCode(EMAIL, "code"), user);
    }

    private void checkUser(Function<UserRepository, Optional<User>> userRepositoryFunction, User expectedUser) {
        userRepositoryFunction.apply(userRepository)
                .ifPresentOrElse(u -> {
                    assertEquals(expectedUser.getId(), u.getId());
                    assertEquals(expectedUser.getEmail(), u.getEmail());
                    assertEquals(expectedUser.getSalt(), u.getSalt());
                    assertEquals(expectedUser.getHash(), u.getHash());
                    assertEquals(expectedUser.getProtectedSymmetricKey(), u.getProtectedSymmetricKey());
                    assertEquals(expectedUser.getInitializationVector(), u.getInitializationVector());
                    assertEquals(getLocalDataTime(expectedUser.getTimestampCreation()), getLocalDataTime(u.getTimestampCreation()));
                    assertEquals(getLocalDataTime(expectedUser.getTimestampLastAccess()), getLocalDataTime(u.getTimestampLastAccess()));
                    assertEquals(getLocalDataTime(expectedUser.getTimestampPassword()), getLocalDataTime(u.getTimestampPassword()));
                    assertEquals(expectedUser.getLanguage(), u.getLanguage());
                    assertEquals(expectedUser.getHint(), u.getHint());
                    assertEquals(expectedUser.getPropic(), u.getPropic());
                    assertEquals(expectedUser.getState(), u.getState());
                    assertEquals(expectedUser.getVerificationCode(), u.getVerificationCode());
                }, Assertions::fail);
    }

}
