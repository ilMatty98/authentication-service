package com.ilmatty98.resource.authentication;


import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.SignUpDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
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
    void testProtectedSymmetricKeyEmpty() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setProtectedSymmetricKey(null);

        given()
                .contentType(ContentType.JSON)
                .body(new SignUpDto())
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testInitializationVectorEmpty() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setInitializationVector(null);

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
    void testSignUp() {
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

        //Check user
        userRepository.findByEmail(signUp.getEmail())
                .ifPresentOrElse(user -> {
                    assertNotNull(user.getId());
                    assertEquals(signUp.getEmail(), user.getEmail());
                    assertNotNull(user.getSalt());
                    assertNotNull(user.getHash());
                    assertEquals(signUp.getProtectedSymmetricKey(), authenticationMapper.base64DecodingString(user.getProtectedSymmetricKey()));
                    assertEquals(signUp.getInitializationVector(), authenticationMapper.base64DecodingString(user.getInitializationVector()));
                    assertNotNull(user.getTimestampCreation());
                    assertNotNull(user.getTimestampLastAccess());
                    assertNotNull(user.getTimestampPassword());
                    assertNotNull(user.getTimestampEmail());
                    assertEquals(signUp.getLanguage(), user.getLanguage());
                    assertEquals(signUp.getHint(), user.getHint());
                    assertEquals(signUp.getPropic(), user.getPropic());
                    assertEquals(UserStateEnum.UNVERIFIED, user.getState());
                    assertNotNull(user.getVerificationCode());
                    assertNull(user.getNewEmail());
                    assertNull(user.getAttempt());
                }, Assertions::fail);
    }

}
