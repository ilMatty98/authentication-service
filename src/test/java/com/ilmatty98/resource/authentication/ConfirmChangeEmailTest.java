package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationCredentialServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.authentication.request.ConfirmChangeEmailDto;
import com.ilmatty98.entity.Account;
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
class ConfirmChangeEmailTest extends AuthenticationCredentialServiceTests {

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
    void testAccountNotFound() {
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
        var account = changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        account = getAccountById(account.getId());
        account.setTimestampEmail(Timestamp.valueOf(account.getTimestampEmail().toLocalDateTime().minusHours(1)));
        deleteAccountById(account.getId());
        saveAccount(account);
        account.setId(account.getId() + 1);

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

        checkAccount(account, EMAIL, null, null, null);
    }

    @Test
    void testLimitAttempts() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var account = changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        account = getAccountById(account.getId());
        account.setAttempt(10);
        deleteAccountById(account.getId());
        saveAccount(account);
        account.setId(account.getId() + 1);

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

        checkAccount(account, EMAIL, null, null, null);
    }

    @Test
    void testIncorrectVerificationCode() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var account = changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        account = getAccountById(account.getId());

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

        checkAccount(account, EMAIL, account.getVerificationCode(), NEW_EMAIL, 1);
    }

    @Test
    void testConfirmChangeEmail() throws MessagingException {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var account = changeEmail(EMAIL, PASSWORD, NEW_EMAIL);

        account = getAccountById(account.getId());

        var confirmChangeEmailDto = new ConfirmChangeEmailDto();
        confirmChangeEmailDto.setEmail(NEW_EMAIL);
        confirmChangeEmailDto.setMasterPasswordHash(PASSWORD);
        confirmChangeEmailDto.setVerificationCode(account.getVerificationCode());
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

        var u = getAccountById(account.getId());
        account.setSalt(u.getSalt());
        account.setHash(u.getHash());
        checkAccount(account, NEW_EMAIL, null, null, null);

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

    private void checkAccount(Account account, String email, String verificationCode, String newEmail, Integer attempt) {
        var u = getAccountById(account.getId());
        assertEquals(account.getId(), u.getId());
        assertEquals(email, u.getEmail());
        assertEquals(account.getSalt(), u.getSalt());
        assertEquals(account.getHash(), u.getHash());
        testBetweenTimestamp(account.getTimestampCreation(), u.getTimestampCreation());
        assertTrue(account.getTimestampLastAccess().before(u.getTimestampLastAccess()));
        testBetweenTimestamp(account.getTimestampPassword(), u.getTimestampPassword());
        assertTrue(u.getTimestampEmail().before(Timestamp.from(Instant.now())));
        assertEquals(account.getLanguage(), u.getLanguage());
        assertEquals(account.getHint(), u.getHint());
        assertEquals(account.getPropic(), u.getPropic());
        assertEquals(AccountStateEnum.VERIFIED, u.getState());
        assertEquals(verificationCode, u.getVerificationCode());
        assertEquals(newEmail, u.getNewEmail());
        assertEquals(attempt, u.getAttempt());
    }

}
