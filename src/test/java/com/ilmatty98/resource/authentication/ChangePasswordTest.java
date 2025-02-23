package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.constants.TokenClaimEnum;
import com.ilmatty98.dto.request.ChangePasswordDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ChangePasswordTest extends AuthenticationServiceTests {

    @Test
    void testWithoutToken() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .put(CHANGE_PASSWORD_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testChangePasswordDtoEmpty() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .contentType(ContentType.JSON)
                .body(new ChangePasswordDto())
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_PASSWORD_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testMasterPasswordHashEmpty() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var changePasswordDto = fillObject(new ChangePasswordDto());
        changePasswordDto.setNewMasterPasswordHash(null);

        given()
                .contentType(ContentType.JSON)
                .body(changePasswordDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_PASSWORD_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testProtectedSymmetricKeyEmpty() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var changePasswordDto = fillObject(new ChangePasswordDto());
        changePasswordDto.setNewProtectedSymmetricKey(null);

        given()
                .contentType(ContentType.JSON)
                .body(changePasswordDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_PASSWORD_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testInitializationVectorEmpty() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var changePasswordDto = fillObject(new ChangePasswordDto());
        changePasswordDto.setNewInitializationVector(null);

        given()
                .contentType(ContentType.JSON)
                .body(changePasswordDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_PASSWORD_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testWithClaimsWithoutEmail() {
        var account = signUp(EMAIL, PASSWORD);
        account = confirmEmail(EMAIL);

        var changePasswordDto = fillObject(new ChangePasswordDto());
        changePasswordDto.setNewMasterPasswordHash("new password");
        changePasswordDto.setNewProtectedSymmetricKey("new protectedSymmetricKey");
        changePasswordDto.setNewInitializationVector("new initializationVector");

        var claims = new HashMap<String, Object>();
        claims.put(TokenClaimEnum.ROLE.getLabel(), account.getState());
        var token = tokenJwtService.generateTokenJwt(claims);

        given()
                .contentType(ContentType.JSON)
                .body(changePasswordDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + token)
                .when()
                .put(CHANGE_PASSWORD_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testEmailNotFound() {
        var account = signUp(EMAIL, PASSWORD);
        account = confirmEmail(EMAIL);

        var changePasswordDto = fillObject(new ChangePasswordDto());
        changePasswordDto.setNewMasterPasswordHash("new password");
        changePasswordDto.setNewProtectedSymmetricKey("new protectedSymmetricKey");
        changePasswordDto.setNewInitializationVector("new initializationVector");

        var claims = new HashMap<String, Object>();
        claims.put(TokenClaimEnum.EMAIL.getLabel(), EMAIL + ".");
        claims.put(TokenClaimEnum.ROLE.getLabel(), account.getState());
        var token = tokenJwtService.generateTokenJwt(claims);

        given()
                .contentType(ContentType.JSON)
                .body(changePasswordDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + token)
                .when()
                .put(CHANGE_PASSWORD_URL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testCheckPasswordFailed() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var changePasswordDto = fillObject(new ChangePasswordDto());
        changePasswordDto.setCurrentMasterPasswordHash(PASSWORD + "1");
        changePasswordDto.setNewMasterPasswordHash("new password");
        changePasswordDto.setNewProtectedSymmetricKey("new protectedSymmetricKey");
        changePasswordDto.setNewInitializationVector("new initializationVector");

        given()
                .contentType(ContentType.JSON)
                .body(changePasswordDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_PASSWORD_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testChangePassword() throws MessagingException {
        signUp(EMAIL, PASSWORD);
        final var account = confirmEmail(EMAIL);

        var changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setCurrentMasterPasswordHash(PASSWORD);
        changePasswordDto.setNewMasterPasswordHash("new password");
        changePasswordDto.setNewProtectedSymmetricKey("new protectedSymmetricKey");
        changePasswordDto.setNewInitializationVector("new initializationVector");

        given()
                .contentType(ContentType.JSON)
                .body(changePasswordDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_PASSWORD_URL)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        var u = getAccountById(account.getId());
        assertNotNull(u);
        assertNotNull(u.getId());
        assertEquals(account.getEmail(), u.getEmail());
        assertNotNull(u.getSalt());
        assertNotNull(u.getHash());
        assertTrue(u.getTimestampPassword().after(account.getTimestampPassword()));
        assertNotNull(u.getTimestampCreation());
        assertNotNull(u.getTimestampLastAccess());
        assertNotNull(u.getTimestampPassword());
        assertNotNull(u.getTimestampEmail());
        assertEquals(account.getLanguage(), u.getLanguage());
        assertEquals(account.getHint(), u.getHint());
        assertEquals(account.getPropic(), u.getPropic());
        assertEquals(AccountStateEnum.VERIFIED, u.getState());
        assertNull(u.getVerificationCode());
        assertNull(u.getNewEmail());
        assertNull(u.getAttempt());

        //Check email
        var receivedMessages = greenMail.getReceivedMessages();
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        assertEquals(3, receivedMessages.length);

        var email = receivedMessages[2];
        assertEquals(1, email.getAllRecipients().length);
        assertEquals(emailFrom, email.getFrom()[0].toString());
        assertEquals(account.getEmail(), email.getAllRecipients()[0].toString());
        assertEquals("Password changed!", email.getSubject());

        assertNotNull(getTokenFromLogIn(EMAIL, "new password"));
    }
}
