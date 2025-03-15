package com.ilmatty98.repository;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
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
        assertFalse(accountRepository.existsByEmail(EMAIL));
        signUp(EMAIL, PASSWORD);
        assertTrue(accountRepository.existsByEmail(EMAIL));
    }

    @Test
    void testFindByEmail() {
        assertFalse(accountRepository.findByEmail(EMAIL).isPresent());
        var account = signUp(EMAIL, PASSWORD);
        checkAccount((accountRepository) -> accountRepository.findByEmail(EMAIL), account);
    }

    @Test
    @Transactional
    void testFindByEmailAndState() {
        var account = signUp(EMAIL, PASSWORD);
        account.setState(AccountStateEnum.UNVERIFIED);
        accountRepository.persist(account);

        assertFalse(accountRepository.findByEmailAndState(EMAIL, AccountStateEnum.VERIFIED).isPresent());

        account.setState(AccountStateEnum.VERIFIED);
        accountRepository.persist(account);

        checkAccount((accountRepository) -> accountRepository.findByEmailAndState(EMAIL, AccountStateEnum.VERIFIED), account);
    }

    @Test
    @Transactional
    void findByEmailAndNewEmailAndState() {
        var newEmail = EMAIL + ".";
        var account = signUp(EMAIL, PASSWORD);
        account.setState(AccountStateEnum.UNVERIFIED);
        account.setNewEmail(newEmail);
        accountRepository.persist(account);

        assertFalse(accountRepository.findByEmailAndNewEmailAndState(EMAIL, newEmail, AccountStateEnum.VERIFIED).isPresent());

        account.setState(AccountStateEnum.VERIFIED);
        accountRepository.persist(account);

        checkAccount((accountRepository) -> accountRepository.findByEmailAndNewEmailAndState(EMAIL, newEmail, AccountStateEnum.VERIFIED), account);
    }

    @Test
    @Transactional
    void testFindByEmailAndVerificationCode() {
        var account = signUp(EMAIL, PASSWORD);
        account.setVerificationCode("code1");
        accountRepository.persist(account);

        assertFalse(accountRepository.findByEmailAndVerificationCode(EMAIL, "code").isPresent());

        account.setVerificationCode("code");
        accountRepository.persist(account);

        checkAccount((accountRepository) -> accountRepository.findByEmailAndVerificationCode(EMAIL, "code"), account);
    }

    private void checkAccount(Function<AccountRepository, Optional<Account>> accountRepositoryFunction, Account expectedAccount) {
        accountRepositoryFunction.apply(accountRepository)
                .ifPresentOrElse(u -> {
                    assertEquals(expectedAccount.getId(), u.getId());
                    assertEquals(expectedAccount.getEmail(), u.getEmail());
                    assertEquals(expectedAccount.getSalt(), u.getSalt());
                    assertEquals(expectedAccount.getHash(), u.getHash());
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
