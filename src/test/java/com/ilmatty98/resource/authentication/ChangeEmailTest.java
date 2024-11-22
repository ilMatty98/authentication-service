package com.ilmatty98.resource.authentication;


import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.ChangeEmailDto;
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
    void testUserNotFoundForEmail() {
        var user = signUp(EMAIL, PASSWORD);
        user = confirmEmail(EMAIL);
        var changeEmailDto = new ChangeEmailDto();
        changeEmailDto.setEmail("test2@test.com");
        changeEmailDto.setMasterPasswordHash(PASSWORD);

        var token = getTokenFromLogIn(EMAIL, PASSWORD);

        deleteUserById(user.getId());
        user.setEmail(EMAIL + ".");
        saveUser(user);

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
    void testUserNotFoundForState() {
        var user = signUp(EMAIL, PASSWORD);
        user = confirmEmail(EMAIL);
        var changeEmailDto = new ChangeEmailDto();
        changeEmailDto.setEmail("test2@test.com");
        changeEmailDto.setMasterPasswordHash(PASSWORD);

        var token = getTokenFromLogIn(EMAIL, PASSWORD);

        deleteUserById(user.getId());
        user.setState(UserStateEnum.UNVERIFIED);
        saveUser(user);

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
        final var user = confirmEmail(EMAIL);
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

        var u = getUserById(user.getId());
        assertEquals(user.getId(), u.getId());
        assertEquals(EMAIL, u.getEmail());
        assertEquals(user.getSalt(), u.getSalt());
        assertEquals(user.getHash(), u.getHash());
        assertEquals(user.getProtectedSymmetricKey(), u.getProtectedSymmetricKey());
        assertEquals(user.getInitializationVector(), u.getInitializationVector());
        assertEquals(getLocalDataTime(user.getTimestampCreation()), getLocalDataTime(u.getTimestampCreation()));
        assertTrue(user.getTimestampLastAccess().before(u.getTimestampLastAccess()));
        assertEquals(getLocalDataTime(user.getTimestampPassword()), getLocalDataTime(u.getTimestampPassword()));
        assertTrue(user.getTimestampEmail().before(u.getTimestampEmail()));
        assertEquals(user.getLanguage(), u.getLanguage());
        assertEquals(user.getHint(), u.getHint());
        assertEquals(user.getPropic(), u.getPropic());
        assertEquals(UserStateEnum.VERIFIED, u.getState());
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
