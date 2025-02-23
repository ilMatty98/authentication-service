package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
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
        var account = signUp(ConfirmEmailTest.EMAIL, PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch(CONFIRM_EMAIL_URL, EMAIL, account.getVerificationCode())
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


        var u = getAccountById(account.getId());
        assertEquals(account.getId(), u.getId());
        assertEquals(account.getEmail(), u.getEmail());
        assertEquals(account.getSalt(), u.getSalt());
        assertEquals(account.getHash(), u.getHash());
        testBetweenTimestamp(account.getTimestampCreation(), u.getTimestampCreation());
        testBetweenTimestamp(account.getTimestampLastAccess(), u.getTimestampLastAccess());
        testBetweenTimestamp(account.getTimestampPassword(), u.getTimestampPassword());
        testBetweenTimestamp(account.getTimestampEmail(), u.getTimestampEmail());
        assertEquals(account.getLanguage(), u.getLanguage());
        assertEquals(account.getHint(), u.getHint());
        assertEquals(account.getPropic(), u.getPropic());
        assertEquals(AccountStateEnum.VERIFIED, u.getState());
        assertNull(u.getVerificationCode());
        assertNull(u.getNewEmail());
        assertNull(u.getAttempt());
    }

    @Test
    void testEmailAlreadyConfirmed() {
        var account = signUp(ConfirmEmailTest.EMAIL, PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch(CONFIRM_EMAIL_URL, EMAIL, account.getVerificationCode())
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .contentType(ContentType.JSON)
                .when()
                .patch(CONFIRM_EMAIL_URL, EMAIL, account.getVerificationCode())
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

}
