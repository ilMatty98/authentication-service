package com.ilmatty98.mapper;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.authentication.request.SignUpDto;
import com.ilmatty98.entity.Account;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;


@QuarkusTest
class AuthenticationMapperTest extends AuthenticationServiceTests {

    @Test
    void testNewAccount() {
        final var secureRandom = new SecureRandom();
        var signUpDto = fillObject(new SignUpDto());

        var salt = new byte[128];
        secureRandom.nextBytes(salt);

        var hash = new byte[256];
        secureRandom.nextBytes(hash);

        var timestamp = Timestamp.from(Instant.now());
        var accountStateEnum = AccountStateEnum.VERIFIED;

        var account = authenticationMapper.newAccount(signUpDto, salt, hash, timestamp, accountStateEnum);

        assertNull(account.getId());
        assertEquals(signUpDto.getEmail(), account.getEmail());
        assertEquals(authenticationMapper.base64Encoding(salt), account.getSalt());
        assertEquals(authenticationMapper.base64Encoding(hash), account.getHash());
        assertEquals(authenticationMapper.base64EncodingString(signUpDto.getProtectedSymmetricKey()), account.getProtectedSymmetricKey());
        assertEquals(authenticationMapper.base64EncodingString(signUpDto.getInitializationVector()), account.getInitializationVector());
        assertEquals(timestamp, account.getTimestampCreation());
        assertEquals(timestamp, account.getTimestampLastAccess());
        assertEquals(timestamp, account.getTimestampPassword());
        assertEquals(signUpDto.getLanguage(), account.getLanguage());
        assertEquals(accountStateEnum, account.getState());
        assertEquals(signUpDto.getHint(), account.getHint());
        assertEquals(signUpDto.getPropic(), account.getPropic());
        assertNotNull(account.getVerificationCode());
    }

    @Test
    void testNewAccessDto() {
        var account = fillObject(new Account());
        var token = generateRandomString(2048);
        var tokenPublicKey = generateRandomString(1024);

        var protectedSymmetricKey = account.getProtectedSymmetricKey();
        var initializationVector = account.getInitializationVector();
        account.setProtectedSymmetricKey(authenticationMapper.base64EncodingString(protectedSymmetricKey));
        account.setInitializationVector(authenticationMapper.base64EncodingString(initializationVector));

        var loginDto = authenticationMapper.newAccessDto(account, token, tokenPublicKey);

        assertEquals(token, loginDto.getToken());
        assertEquals(tokenPublicKey, loginDto.getTokenPublicKey());
        assertEquals(protectedSymmetricKey, loginDto.getProtectedSymmetricKey());
        assertEquals(initializationVector, loginDto.getInitializationVector());
        assertEquals(account.getLanguage(), loginDto.getLanguage());
        assertEquals(account.getPropic(), loginDto.getPropic());
        assertEquals(account.getTimestampCreation(), loginDto.getTimestampCreation());
        assertEquals(account.getTimestampLastAccess(), loginDto.getTimestampLastAccess());
        assertEquals(account.getTimestampPassword(), loginDto.getTimestampPassword());
    }

    @Test
    void testBase64Encoding() {
        // Test with non-null input
        var input1 = "Hello, world!".getBytes();
        var expectedOutput1 = Base64.getEncoder().encodeToString(input1);
        var actualOutput1 = authenticationMapper.base64Encoding(input1);
        assertEquals(expectedOutput1, actualOutput1);

        // Test with null input
        var actualOutput2 = authenticationMapper.base64Encoding(null);
        assertNull(actualOutput2);
    }

    @Test
    void testBase64Decoding() {
        // Test with non-null input
        var input = "SGVsbG8gV29ybGQh";
        var expectedOutput1 = "Hello World!".getBytes();
        var actualOutput1 = authenticationMapper.base64Decoding(input);
        assertArrayEquals(expectedOutput1, actualOutput1);

        // Test with null input
        var actualOutput2 = authenticationMapper.base64Decoding(null);
        assertNull(actualOutput2);
    }

    @Test
    void testBase64EncodingString() {
        // Test with non-null input
        var input = "Hello World!";
        var expectedOutput1 = "SGVsbG8gV29ybGQh";
        var actualOutput1 = authenticationMapper.base64EncodingString(input);
        assertEquals(expectedOutput1, actualOutput1);

        // Test with null input
        var actualOutput2 = authenticationMapper.base64EncodingString(null);
        assertNull(actualOutput2);
    }

    @Test
    void testBase64DecodingString() {
        // Test with non-null input
        var input = "SGVsbG8gV29ybGQh";
        var expectedOutput1 = "Hello World!";
        var actualOutput1 = authenticationMapper.base64DecodingString(input);
        assertEquals(expectedOutput1, actualOutput1);

        // Test with null input
        var actualOutput2 = authenticationMapper.base64DecodingString(null);
        assertNull(actualOutput2);
    }

    @Test
    void testGetUUID() {
        var uuid = authenticationMapper.getUUID();
        assertNotNull(uuid);
        assertEquals(36, uuid.length());
        assertTrue(uuid.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"));
    }

}
