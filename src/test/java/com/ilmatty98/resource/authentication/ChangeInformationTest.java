package com.ilmatty98.resource.authentication;

import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.authentication.request.ChangeInformationDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ChangeInformationTest extends AuthenticationServiceTests {

    @Test
    void testWithoutToken() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .put(CHANGE_INFORMATION_URL)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testLanguageEmpty() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .contentType(ContentType.JSON)
                .body(new ChangeInformationDto())
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_INFORMATION_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testLanguageNotValid() {
        var changeInformationDto = new ChangeInformationDto();
        changeInformationDto.setLanguage("AAA");

        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .contentType(ContentType.JSON)
                .body(changeInformationDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_INFORMATION_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testHintTooLong() {
        var changeInformationDto = new ChangeInformationDto();
        changeInformationDto.setLanguage(EN);
        changeInformationDto.setHint(generateRandomString(101));

        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .contentType(ContentType.JSON)
                .body(changeInformationDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_INFORMATION_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testPropicEmpty() {
        var changeInformationDto = new ChangeInformationDto();
        changeInformationDto.setLanguage("FR");
        changeInformationDto.setHint("hint");
        changeInformationDto.setPropic(null);

        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        given()
                .contentType(ContentType.JSON)
                .body(changeInformationDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_INFORMATION_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testAccountNotFoundForEmail() {
        var account = signUp(EMAIL, PASSWORD);
        account = confirmEmail(EMAIL);
        var token = getTokenFromLogIn(EMAIL, PASSWORD);

        var changeInformationDto = new ChangeInformationDto();
        changeInformationDto.setLanguage("FR");
        changeInformationDto.setHint("hint");
        changeInformationDto.setPropic("propic");

        account.setEmail(EMAIL + ".");
        deleteAccountById(account.getId());
        saveAccount(account);

        given()
                .contentType(ContentType.JSON)
                .body(changeInformationDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + token)
                .when()
                .put(CHANGE_INFORMATION_URL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testAccountNotFoundForState() {
        var account = signUp(EMAIL, PASSWORD);
        account = confirmEmail(EMAIL);
        var token = getTokenFromLogIn(EMAIL, PASSWORD);

        var changeInformationDto = new ChangeInformationDto();
        changeInformationDto.setLanguage("FR");
        changeInformationDto.setHint("hint");
        changeInformationDto.setPropic("propic");

        account.setState(AccountStateEnum.UNVERIFIED);
        deleteAccountById(account.getId());
        saveAccount(account);

        given()
                .contentType(ContentType.JSON)
                .body(changeInformationDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + token)
                .when()
                .put(CHANGE_INFORMATION_URL)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void testMaxSizePropic() {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);

        var changeInformationDto = new ChangeInformationDto();
        changeInformationDto.setLanguage("FR");
        changeInformationDto.setHint("new hint");
        changeInformationDto.setPropic(createLargeString(4.1));

        given()
                .contentType(ContentType.JSON)
                .body(changeInformationDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_INFORMATION_URL)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testChangeInformation() {
        signUp(EMAIL, PASSWORD);
        final var account = confirmEmail(EMAIL);

        var changeInformationDto = new ChangeInformationDto();
        changeInformationDto.setLanguage("FR");
        changeInformationDto.setHint("new hint");
        changeInformationDto.setPropic("new propic");

        given()
                .contentType(ContentType.JSON)
                .body(changeInformationDto)
                .header(AUTH_HEADER_NAME, AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD))
                .when()
                .put(CHANGE_INFORMATION_URL)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        //Check account
        var u = getAccountById(account.getId());
        assertNotNull(u.getId());
        assertEquals(EMAIL, u.getEmail());
        assertNotNull(u.getSalt());
        assertNotNull(u.getHash());
        assertEquals(account.getProtectedSymmetricKey(), u.getProtectedSymmetricKey());
        assertEquals(account.getInitializationVector(), u.getInitializationVector());
        testBetweenTimestamp(account.getTimestampCreation(), u.getTimestampCreation());
        testBetweenTimestamp(account.getTimestampPassword(), u.getTimestampPassword());
        testBetweenTimestamp(account.getTimestampEmail(), u.getTimestampEmail());
        assertTrue(account.getTimestampLastAccess().before(u.getTimestampLastAccess()));
        assertEquals(changeInformationDto.getLanguage(), u.getLanguage());
        assertEquals(changeInformationDto.getHint(), u.getHint());
        assertEquals(changeInformationDto.getPropic(), u.getPropic());
        assertEquals(account.getState(), u.getState());
        assertNull(u.getVerificationCode());
        assertNull(u.getNewEmail());
        assertNull(u.getAttempt());
    }
}
