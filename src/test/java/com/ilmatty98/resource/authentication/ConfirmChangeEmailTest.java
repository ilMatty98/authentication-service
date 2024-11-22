package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.ConfirmChangeEmailDto;
import com.ilmatty98.entity.User;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ConfirmChangeEmailTest extends AuthenticationServiceTests {

    private static final String NEW_EMAIL = "new" + EMAIL;

    @Test
    void testWithoutToken() {
        given()
                .contentType(ContentType.JSON)
                .body(new ConfirmChangeEmailDto())
                .when()
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testConfirmChangeEmailDtoEmpty() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .contentType(ContentType.JSON)
                .body(new ConfirmChangeEmailDto())
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testEmailNull() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testMasterPasswordHashNull() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(EMAIL);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testVerificationCodeNull() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testNewMasterPasswordHashNull() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testNewProtectedSymmetricKeyNull() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testNewInitializationVectorNull() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testNewEmailAlreadyExist() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        signUp(EMAIL + "a", PASSWORD);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(EMAIL + "a");
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testUserNotFound() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(NEW_EMAIL + "a");
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testPasswordDoesNotCoincide() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(NEW_EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD + "a");
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testTimeOut() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var user = changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        user = getUserById(user.getId());
        user.setTimestampEmail(Timestamp.valueOf(user.getTimestampEmail().toLocalDateTime().minusHours(1)));
        deleteUserById(user.getId());
        saveUser(user);
        user.setId(user.getId() + 1);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(NEW_EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        checkUser(user, EMAIL, null, null, null);
    }

    @Test
    void testLimitAttempts() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var user = changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        user = getUserById(user.getId());
        user.setAttempt(10);
        deleteUserById(user.getId());
        saveUser(user);
        user.setId(user.getId() + 1);

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(NEW_EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        checkUser(user, EMAIL, null, null, null);
    }

    @Test
    void testIncorrectVerificationCode() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var user = changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        user = getUserById(user.getId());

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(NEW_EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode("vc");
        confirmChangeEmailDto.setNewMasterPasswordHash("mp");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("ps");
        confirmChangeEmailDto.setNewInitializationVector("iv");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        checkUser(user, EMAIL, user.getVerificationCode(), NEW_EMAIL, 1);
    }

    @Test
    void testConfirmChangeEmail() throws MessagingException {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var user = changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        user = getUserById(user.getId());

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(NEW_EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode(user.getVerificationCode());
        confirmChangeEmailDto.setNewMasterPasswordHash("new masterPasswordHash");
        confirmChangeEmailDto.setNewProtectedSymmetricKey("new protectedSymmetricKey");
        confirmChangeEmailDto.setNewInitializationVector("new initializationVector");

        given()
                .contentType(ContentType.JSON)
                .body(confirmChangeEmailDto)
                .when()
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .put(CONFIRM_CHANGE_EMAIL_URL)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        var u = getUserById(user.getId());
        user.setSalt(u.getSalt());
        user.setHash(u.getHash());
        user.setProtectedSymmetricKey(authenticationMapper.base64EncodingString("new protectedSymmetricKey"));
        user.setInitializationVector(authenticationMapper.base64EncodingString("new initializationVector"));
        checkUser(user, NEW_EMAIL, null, null, null);

        assertNotNull(getTokenFromLogIn(NEW_EMAIL, "new masterPasswordHash"));

        //Check email
        var receivedMessages = greenMail.getReceivedMessages();
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        assertEquals(7, receivedMessages.length);

        var emailChanged = receivedMessages[5];
        assertEquals(1, emailChanged.getAllRecipients().length);
        assertEquals(emailFrom, emailChanged.getFrom()[0].toString());
        assertEquals(NEW_EMAIL, emailChanged.getAllRecipients()[0].toString());
        assertEquals("Email changed!", emailChanged.getSubject());
    }

    private void checkUser(User user, String email, String verificationCode, String newEmail, Integer attempt) {
        var u = getUserById(user.getId());
        assertEquals(user.getId(), u.getId());
        assertEquals(email, u.getEmail());
        assertEquals(user.getSalt(), u.getSalt());
        assertEquals(user.getHash(), u.getHash());
        assertEquals(user.getProtectedSymmetricKey(), u.getProtectedSymmetricKey());
        assertEquals(user.getInitializationVector(), u.getInitializationVector());
        testBetweenTimestamp(user.getTimestampCreation(), u.getTimestampCreation());
        assertTrue(user.getTimestampLastAccess().before(u.getTimestampLastAccess()));
        testBetweenTimestamp(user.getTimestampPassword(), u.getTimestampPassword());
        assertTrue(u.getTimestampEmail().before(Timestamp.from(Instant.now())));
        assertEquals(user.getLanguage(), u.getLanguage());
        assertEquals(user.getHint(), u.getHint());
        assertEquals(user.getPropic(), u.getPropic());
        assertEquals(UserStateEnum.VERIFIED, u.getState());
        assertEquals(verificationCode, u.getVerificationCode());
        assertEquals(newEmail, u.getNewEmail());
        assertEquals(attempt, u.getAttempt());
    }

}
