package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class SendHintTest extends AuthenticationServiceTests {

    @Test
    void testWithoutEmail() {
        given()
                .when()
                .post("/v1/authentication/sendHint/")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testNotFoundForEmail() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .when()
                .post(SEND_HINT_URL, EMAIL + ".")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testNotFoundForState() {
        signUp(EMAIL, PASSWORD);

        given()
                .when()
                .post(SEND_HINT_URL, EMAIL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testSendHint() throws MessagingException {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .when()
                .post(SEND_HINT_URL, EMAIL)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        //Check email
        var receivedMessages = greenMail.getReceivedMessages();
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        assertEquals(2, receivedMessages.length);

        var email = receivedMessages[1];
        assertEquals(1, email.getAllRecipients().length);
        assertEquals(emailFrom, email.getFrom()[0].toString());
        assertEquals(EMAIL, email.getAllRecipients()[0].toString());
        assertEquals("Your Master Password Hint", email.getSubject());
    }

}
