package com.ilmatty98.resource.authentication;


import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.authentication.request.SignUpDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SignUpTest extends AuthenticationServiceTests {

    @Test
    void testSignUpDtoEmpty() {
        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testEmailEmpty() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(null);

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testEmailNotValid() {
        var signUp = fillObject(new SignUpDto());

        given()
                .contentType(ContentType.JSON)
                .body(signUp)
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testMasterPasswordHashEmpty() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setMasterPasswordHash(null);

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testHintEmpty() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setHint(null);

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testHintTooLong() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setHint(generateRandomString(101));
        signUp.setLanguage(EN);

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testLanguageEmpty() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setLanguage(null);

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testLanguageNotValid() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setLanguage("as");

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testPropicEmpty() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setLanguage(EN);
        signUp.setPropic(null);

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testEmailAlreadyRegistered() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setLanguage(EN);

        signUp(signUp.getEmail(), PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(signUp)
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testMaxSizePropic() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setLanguage(EN);
        signUp.setPropic(createLargeString(4.1));

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testAlreadySignUp() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(PASSWORD);
        signUp.setLanguage(EN);

        given()
                .contentType(ContentType.JSON)
                .body(signUp)
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testSignUp() throws MessagingException {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setLanguage(EN);

        given()
                .contentType(ContentType.JSON)
                .body(signUp)
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        //Check account
        accountRepository.findByEmail(signUp.getEmail())
                .ifPresentOrElse(account -> {
                    assertNotNull(account.getId());
                    assertEquals(signUp.getEmail(), account.getEmail());
                    assertNotNull(account.getSalt());
                    assertNotNull(account.getHash());
                    assertNotNull(account.getTimestampCreation());
                    assertNotNull(account.getTimestampLastAccess());
                    assertNotNull(account.getTimestampPassword());
                    assertNotNull(account.getTimestampEmail());
                    assertEquals(signUp.getLanguage(), account.getLanguage());
                    assertEquals(signUp.getHint(), account.getHint());
                    assertEquals(signUp.getPropic(), account.getPropic());
                    assertEquals(AccountStateEnum.UNVERIFIED, account.getState());
                    assertNotNull(account.getVerificationCode());
                    assertNull(account.getNewEmail());
                    assertNull(account.getAttempt());
                }, Assertions::fail);

        //Check email
        var receivedMessages = greenMail.getReceivedMessages();
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        assertEquals(1, receivedMessages.length);

        var email = receivedMessages[0];
        assertEquals(1, email.getAllRecipients().length);
        assertEquals(emailFrom, email.getFrom()[0].toString());
        assertEquals(signUp.getEmail(), email.getAllRecipients()[0].toString());
        assertEquals("Welcome to Credentials Manager!", email.getSubject());
    }

}
