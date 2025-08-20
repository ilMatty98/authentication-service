package com.ilmatty98.resource.authentication;


import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.authentication.request.ChangeEmailDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ChangeEmailTest extends AuthenticationServiceTests {

    @Test
    void testWithoutToken() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .put(CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testChangeEmailDtoEmpty() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .contentType(ContentType.JSON)
                .body(new ChangeEmailDto())
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testNewEmailNotValid() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var changeEmailDto = new ChangeEmailDto();
        changeEmailDto.setEmail("aaaa");
        changeEmailDto.setMasterPasswordHash(PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(changeEmailDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testAccountNotFoundForEmail() {
        var account = signUp(EMAIL, PASSWORD);
        account = confirmEmail(EMAIL);
        var changeEmailDto = new ChangeEmailDto();
        changeEmailDto.setEmail("test2@test.com");
        changeEmailDto.setMasterPasswordHash(PASSWORD);

        var token = getTokenFromLogIn(EMAIL, PASSWORD);

        deleteAccountById(account.getId());
        account.setEmail(EMAIL + ".");
        saveAccount(account);

        given()
                .contentType(ContentType.JSON)
                .body(changeEmailDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + token)
                .when()
                .put(CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testAccountNotFoundForState() {
        var account = signUp(EMAIL, PASSWORD);
        account = confirmEmail(EMAIL);
        var changeEmailDto = new ChangeEmailDto();
        changeEmailDto.setEmail("test2@test.com");
        changeEmailDto.setMasterPasswordHash(PASSWORD);

        var token = getTokenFromLogIn(EMAIL, PASSWORD);

        deleteAccountById(account.getId());
        account.setState(AccountStateEnum.UNVERIFIED);
        saveAccount(account);

        given()
                .contentType(ContentType.JSON)
                .body(changeEmailDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + token)
                .when()
                .put(CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testIncorrectCurrentPasswordEmpty() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var changeEmailDto = new ChangeEmailDto();
        changeEmailDto.setEmail("test2@test.com");
        changeEmailDto.setMasterPasswordHash(PASSWORD + ".");

        given()
                .contentType(ContentType.JSON)
                .body(changeEmailDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testNewEmailAlreadyPresent() {
        var newEmail = "test2@test.com";
        signUp(newEmail, PASSWORD);
        confirmEmail(newEmail);

        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var changeEmailDto = new ChangeEmailDto();
        changeEmailDto.setEmail(newEmail);
        changeEmailDto.setMasterPasswordHash(PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(changeEmailDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testNewEmailEqualToThePreviousOne() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var changeEmailDto = new ChangeEmailDto();
        changeEmailDto.setEmail(EMAIL);
        changeEmailDto.setMasterPasswordHash(PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(changeEmailDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testChangeEmail() throws MessagingException {
        var newEmail = "test2@test.com";
        signUp(EMAIL, PASSWORD);
        final var account = confirmEmail(EMAIL);
        var changeEmailDto = new ChangeEmailDto();
        changeEmailDto.setEmail(newEmail);
        changeEmailDto.setMasterPasswordHash(PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(changeEmailDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        var u = getAccountById(account.getId());
        assertEquals(account.getId(), u.getId());
        assertEquals(EMAIL, u.getEmail());
        assertEquals(account.getSalt(), u.getSalt());
        assertEquals(account.getHash(), u.getHash());
        assertEquals(account.getProtectedSymmetricKey(), u.getProtectedSymmetricKey());
        assertEquals(account.getInitializationVector(), u.getInitializationVector());
        testBetweenTimestamp(account.getTimestampCreation(), u.getTimestampCreation());
        assertTrue(account.getTimestampLastAccess().before(u.getTimestampLastAccess()));
        testBetweenTimestamp(account.getTimestampPassword(), u.getTimestampPassword());
        assertTrue(account.getTimestampEmail().before(u.getTimestampEmail()));
        assertEquals(account.getLanguage(), u.getLanguage());
        assertEquals(account.getHint(), u.getHint());
        assertEquals(account.getPropic(), u.getPropic());
        assertEquals(AccountStateEnum.VERIFIED, u.getState());
        assertNotNull(u.getVerificationCode());
        assertEquals(newEmail, u.getNewEmail());
        assertEquals(0, u.getAttempt());


        //Check email
        var receivedMessages = greenMail.getReceivedMessages();
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        assertEquals(4, receivedMessages.length);

        var emailNotification = receivedMessages[2];
        assertEquals(1, emailNotification.getAllRecipients().length);
        assertEquals(emailFrom, emailNotification.getFrom()[0].toString());
        assertEquals(EMAIL, emailNotification.getAllRecipients()[0].toString());
        assertEquals("Your Email Change!", emailNotification.getSubject());

        var emailCode = receivedMessages[3];
        assertEquals(1, emailCode.getAllRecipients().length);
        assertEquals(emailFrom, emailCode.getFrom()[0].toString());
        assertEquals(newEmail, emailCode.getAllRecipients()[0].toString());
        assertEquals("Your Email Change!", emailCode.getSubject());
    }
}
