package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.dto.request.SignUpDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CheckEmailTest extends AuthenticationServiceTests {
    private static final String IT = "IT";

    @Test
    void testWithoutHeader() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/authentication/checkEmail")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testEmailPresent() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setLanguage(IT);
        signUp(signUp.getEmail(), PASSWORD);

        var response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(CHECK_EMAIL_URL, signUp.getEmail())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .asString();

        assertTrue(Boolean.parseBoolean(response));
    }

    @Test
    void testEmailNotPresent() {
        var signUp = fillObject(new SignUpDto());
        signUp.setEmail(EMAIL);
        signUp.setLanguage(IT);
        signUp(signUp.getEmail(), PASSWORD);

        var response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(CHECK_EMAIL_URL, signUp.getEmail() + "fake")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .asString();

        assertFalse(Boolean.parseBoolean(response));
    }
}
