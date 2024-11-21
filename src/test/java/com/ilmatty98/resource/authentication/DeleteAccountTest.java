package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.DeleteDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class DeleteAccountTest extends AuthenticationServiceTests {

    @Test
    void testWithoutToken() {
        var dto = new DeleteDto();
        dto.setMasterPasswordHash("AAA");

        given()
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .delete(DELETE_ACCOUNT_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testUserNotFoundForEmail() {
        var user = signUp(EMAIL, PASSWORD);
        user = confirmEmail(EMAIL);
        var token = getTokenFromLogIn(EMAIL, PASSWORD);

        user.setEmail(EMAIL + ".");
        deleteUserById(user.getId());
        saveUser(user);

        var dto = new DeleteDto();
        dto.setMasterPasswordHash("AAA");

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + token)
                .body(dto)
                .when()
                .delete(DELETE_ACCOUNT_URL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testUserNotFoundForState() {
        var user = signUp(EMAIL, PASSWORD);
        user = confirmEmail(EMAIL);
        var token = getTokenFromLogIn(EMAIL, PASSWORD);

        user.setState(UserStateEnum.UNVERIFIED);
        deleteUserById(user.getId());
        saveUser(user);

        var dto = new DeleteDto();
        dto.setMasterPasswordHash("AAA");

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + token)
                .body(dto)
                .when()
                .delete(DELETE_ACCOUNT_URL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testIncorrectPassword() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var dto = new DeleteDto();
        dto.setMasterPasswordHash(PASSWORD + "A");

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .body(dto)
                .when()
                .delete(DELETE_ACCOUNT_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testDeleteUser() throws MessagingException {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var dto = new DeleteDto();
        dto.setMasterPasswordHash(PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .body(dto)
                .when()
                .delete(DELETE_ACCOUNT_URL)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        assertTrue(userRepository.findByEmail(EMAIL).isEmpty());

        //Check email
        var receivedMessages = greenMail.getReceivedMessages();
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        assertEquals(3, receivedMessages.length);

        var email = receivedMessages[2];
        assertEquals(1, email.getAllRecipients().length);
        assertEquals(emailFrom, email.getFrom()[0].toString());
        assertEquals(EMAIL, email.getAllRecipients()[0].toString());
        assertEquals("Successfully deleted your Credential Manager account!", email.getSubject());
    }

}
