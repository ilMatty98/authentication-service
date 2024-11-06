package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.dto.request.LogInDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

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

//        Non riesco a recuperare il contesto di aggiornato da rest assured
//        userRepository.findByEmail(EMAIL).ifPresentOrElse(u -> {
//            assertEquals(user.getId(), u.getId());
//            assertEquals(user.getEmail(), u.getEmail());
//            assertEquals(user.getSalt(), u.getSalt());
//            assertEquals(user.getHash(), u.getHash());
//            assertEquals(user.getProtectedSymmetricKey(), u.getProtectedSymmetricKey());
//            assertEquals(user.getInitializationVector(), u.getInitializationVector());
//            assertEquals(getLocalDataTime(user.getTimestampCreation()), getLocalDataTime(u.getTimestampCreation()));
//            assertEquals(getLocalDataTime(user.getTimestampLastAccess()), getLocalDataTime(u.getTimestampLastAccess()));
//            assertEquals(getLocalDataTime(user.getTimestampPassword()), getLocalDataTime(u.getTimestampPassword()));
//            assertEquals(getLocalDataTime(user.getTimestampEmail()), getLocalDataTime(u.getTimestampEmail()));
//            assertEquals(user.getLanguage(), u.getLanguage());
//            assertEquals(user.getHint(), u.getHint());
//            assertEquals(user.getPropic(), u.getPropic());
//            assertEquals(UserStateEnum.VERIFIED, u.getState());
//            assertNull(u.getVerificationCode());
//            assertNull(u.getNewEmail());
//            assertNull(u.getAttempt());
//        }, Assertions::fail);
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
