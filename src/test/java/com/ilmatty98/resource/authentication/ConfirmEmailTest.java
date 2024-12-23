package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.LogInDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class ConfirmEmailTest extends AuthenticationServiceTests {

    @Test
    void testEmailNotFound() {
        signUp(ConfirmEmailTest.EMAIL, PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch(CONFIRM_EMAIL_URL, EMAIL + ".", CODE)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testCodeNotFound() {
        signUp(ConfirmEmailTest.EMAIL, PASSWORD);
        given()
                .contentType(ContentType.JSON)
                .when()
                .patch(CONFIRM_EMAIL_URL, EMAIL, CODE + ".")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testConfirmEmail() {
        var user = signUp(ConfirmEmailTest.EMAIL, PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch(CONFIRM_EMAIL_URL, EMAIL, user.getVerificationCode())
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        var logIn = fillObject(new LogInDto());
        logIn.setEmail(EMAIL);
        logIn.setIpAddress(IP_ADDRESS);
        logIn.setMasterPasswordHash(PASSWORD);
        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());


        var u = getUserById(user.getId());
        assertEquals(user.getId(), u.getId());
        assertEquals(user.getEmail(), u.getEmail());
        assertEquals(user.getSalt(), u.getSalt());
        assertEquals(user.getHash(), u.getHash());
        assertEquals(user.getProtectedSymmetricKey(), u.getProtectedSymmetricKey());
        assertEquals(user.getInitializationVector(), u.getInitializationVector());
        testBetweenTimestamp(user.getTimestampCreation(), u.getTimestampCreation());
        testBetweenTimestamp(user.getTimestampLastAccess(), u.getTimestampLastAccess());
        testBetweenTimestamp(user.getTimestampPassword(), u.getTimestampPassword());
        testBetweenTimestamp(user.getTimestampEmail(), u.getTimestampEmail());
        assertEquals(user.getLanguage(), u.getLanguage());
        assertEquals(user.getHint(), u.getHint());
        assertEquals(user.getPropic(), u.getPropic());
        assertEquals(UserStateEnum.VERIFIED, u.getState());
        assertNull(u.getVerificationCode());
        assertNull(u.getNewEmail());
        assertNull(u.getAttempt());
    }

    @Test
    void testEmailAlreadyConfirmed() {
        var user = signUp(ConfirmEmailTest.EMAIL, PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch(CONFIRM_EMAIL_URL, EMAIL, user.getVerificationCode())
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch(CONFIRM_EMAIL_URL, EMAIL, user.getVerificationCode())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

}
