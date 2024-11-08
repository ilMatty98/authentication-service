package com.ilmatty98.resource.authentication;


import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.dto.request.LogInDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class LogInTest extends AuthenticationServiceTests {

    @Test
    void testLogInDtoEmpty() {
        given()
                .contentType(ContentType.JSON)
                .body(new LogInDto())
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testEmailEmpty() {
        var logIn = fillObject(new LogInDto());
        logIn.setEmail(null);

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testEmailNotValid() {
        var logIn = fillObject(new LogInDto());

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testMasterPasswordHashEmpty() {
        var logIn = fillObject(new LogInDto());
        logIn.setEmail(EMAIL);
        logIn.setMasterPasswordHash(null);

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testIpAddressEmpty() {
        var logIn = fillObject(new LogInDto());
        logIn.setEmail(EMAIL);
        logIn.setIpAddress(null);

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testIpAddressNotValid() {
        var logIn = fillObject(new LogInDto());
        logIn.setEmail(EMAIL);
        logIn.setIpAddress("fakeip");

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testDeviceTypeEmpty() {
        var logIn = fillObject(new LogInDto());
        logIn.setEmail(EMAIL);
        logIn.setIpAddress(IP_ADDRESS);
        logIn.setDeviceType(null);

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testLocalDateTimeEmpty() {
        var logIn = fillObject(new LogInDto());
        logIn.setEmail(EMAIL);
        logIn.setIpAddress(IP_ADDRESS);
        logIn.setLocalDateTime(null);

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testUserNotFound() {
        signUp(EMAIL, PASSWORD);
        var logIn = fillObject(new LogInDto());
        logIn.setEmail("a" + EMAIL);
        logIn.setIpAddress(IP_ADDRESS);

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testUserUnverified() {
        var user = signUp(EMAIL, PASSWORD);

        var logIn = fillObject(new LogInDto());
        logIn.setEmail(user.getEmail());
        logIn.setIpAddress(IP_ADDRESS);

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testMasterPasswordHashDifferent() {
        var logIn = fillObject(new LogInDto());
        logIn.setEmail(EMAIL);
        logIn.setIpAddress(IP_ADDRESS);
        logIn.setMasterPasswordHash(PASSWORD + ".");

        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testLogIn() {
        var user = signUp(EMAIL, PASSWORD);
        user = confirmEmail(EMAIL);

        var logIn = fillObject(new LogInDto());
        logIn.setEmail(EMAIL);
        logIn.setIpAddress(IP_ADDRESS);
        logIn.setMasterPasswordHash(PASSWORD);

        var response = given()
                .contentType(ContentType.JSON)
                .body(logIn)
                .when()
                .post(LOG_IN_URL);

        response.then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("token", not(emptyOrNullString()))
                .body("tokenPublicKey", equalTo(tokenJwtService.getPublicKey()))
                .body("protectedSymmetricKey", equalTo(authenticationMapper.base64DecodingString(user.getProtectedSymmetricKey())))
                .body("initializationVector", equalTo(authenticationMapper.base64DecodingString(user.getInitializationVector())))
                .body("language", equalTo(user.getLanguage()))
                .body("propic", equalTo(user.getPropic()))
                .body("hint", equalTo(user.getHint()))
                .body("timestampCreation", not(emptyOrNullString()))
                .body("timestampLastAccess", not(emptyOrNullString()))
                .body("timestampPassword", not(emptyOrNullString()))
                .body("timestampEmail", not(emptyOrNullString()));

        var timestampCreation = Timestamp.valueOf(OffsetDateTime.parse(response.jsonPath().getString("timestampCreation")).toLocalDateTime());
        var timestampLastAccess = Timestamp.valueOf(OffsetDateTime.parse(response.jsonPath().getString("timestampLastAccess")).toLocalDateTime());

        assertTrue(timestampLastAccess.after(timestampCreation));

//        //Check email
//        var receivedMessages = greenMail.getReceivedMessages();
//        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
//        assertEquals(2, receivedMessages.length);
//
//        var email = receivedMessages[1];
//        assertEquals(1, email.getAllRecipients().length);
//        assertEquals(emailFrom, email.getFrom()[0].toString());
//        assertEquals(EMAIL, email.getAllRecipients()[0].toString());
//        assertEquals("New access on Credential Manager!", email.getSubject());
    }

}
