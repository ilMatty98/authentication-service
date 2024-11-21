package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.TokenClaimEnum;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.ChangePasswordDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
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
        var user = signUp(EMAIL, PASSWORD);
        user = confirmEmail(EMAIL);

        var changePasswordDto = fillObject(new ChangePasswordDto());
        changePasswordDto.setNewMasterPasswordHash("new password");
        changePasswordDto.setNewProtectedSymmetricKey("new protectedSymmetricKey");
        changePasswordDto.setNewInitializationVector("new initializationVector");

        var claims = new HashMap<String, Object>();
        claims.put(TokenClaimEnum.ROLE.getLabel(), user.getState());
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
        var user = signUp(EMAIL, PASSWORD);
        user = confirmEmail(EMAIL);

        var changePasswordDto = fillObject(new ChangePasswordDto());
        changePasswordDto.setNewMasterPasswordHash("new password");
        changePasswordDto.setNewProtectedSymmetricKey("new protectedSymmetricKey");
        changePasswordDto.setNewInitializationVector("new initializationVector");

        var claims = new HashMap<String, Object>();
        claims.put(TokenClaimEnum.EMAIL.getLabel(), EMAIL + ".");
        claims.put(TokenClaimEnum.ROLE.getLabel(), user.getState());
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
        final var user = confirmEmail(EMAIL);

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

        var u = getUserById(user.getId());
        assertNotNull(u);
        assertNotNull(u.getId());
        assertEquals(user.getEmail(), u.getEmail());
        assertNotNull(u.getSalt());
        assertNotNull(u.getHash());
        assertEquals(changePasswordDto.getNewProtectedSymmetricKey(), authenticationMapper.base64DecodingString(u.getProtectedSymmetricKey()));
        assertEquals(changePasswordDto.getNewInitializationVector(), authenticationMapper.base64DecodingString(u.getInitializationVector()));
        assertTrue(u.getTimestampPassword().after(user.getTimestampPassword()));
        assertNotNull(u.getTimestampCreation());
        assertNotNull(u.getTimestampLastAccess());
        assertNotNull(u.getTimestampPassword());
        assertNotNull(u.getTimestampEmail());
        assertEquals(user.getLanguage(), u.getLanguage());
        assertEquals(user.getHint(), u.getHint());
        assertEquals(user.getPropic(), u.getPropic());
        assertEquals(UserStateEnum.VERIFIED, u.getState());
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
        assertEquals(user.getEmail(), email.getAllRecipients()[0].toString());
        assertEquals("Password changed!", email.getSubject());

        assertNotNull(getTokenFromLogIn(EMAIL, "new password"));
    }
}
