package com.ilmatty98.resource.vault;


import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.vault.CardDto;
import com.ilmatty98.dto.vault.CredentialDto;
import com.ilmatty98.dto.vault.VaultDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.ilmatty98.constants.UrlConstants.Card.BASE_PATH_CARD;
import static com.ilmatty98.constants.UrlConstants.Credential.BASE_PATH_CREDENTIAL;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class InsertTest extends AuthenticationServiceTests {

    @ParameterizedTest
    @ValueSource(strings = {BASE_PATH_CREDENTIAL, BASE_PATH_CARD})
    void testWithoutToken(String url) {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {BASE_PATH_CREDENTIAL, BASE_PATH_CARD})
    void testAccountNotFound(String url) {
        var account = signUp(EMAIL, PASSWORD);
        account = confirmEmail(EMAIL);
        var header = AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD);
        deleteAccountById(account.getId());

        given()
                .contentType(ContentType.JSON)
                .when()
                .header(AUTH_HEADER_NAME, header)
                .body(getDtoFilled(url))
                .post(url)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {BASE_PATH_CREDENTIAL, BASE_PATH_CARD})
    void testAccountNotVerified(String url) {
        var account = signUp(EMAIL, PASSWORD);
        account = confirmEmail(EMAIL);
        var header = AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD);

        account.setState(AccountStateEnum.UNVERIFIED);
        editAccount(account);

        given()
                .contentType(ContentType.JSON)
                .when()
                .header(AUTH_HEADER_NAME, header)
                .body(getDtoFilled(url))
                .post(url)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {BASE_PATH_CREDENTIAL, BASE_PATH_CARD})
    void testInsert(String url) {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var header = AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD);

        if (BASE_PATH_CREDENTIAL.equals(url)) {
            var dto = fillObject(new CredentialDto());

            var credential = given()
                    .contentType(ContentType.JSON)
                    .header(AUTH_HEADER_NAME, header)
                    .body(dto)
                    .post(url)
                    .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .extract()
                    .as(CredentialDto.class);


            assertNotNull(credential.getId());
            assertEquals(dto.getName(), credential.getName());
            assertEquals(dto.getNotes(), credential.getNotes());
            testBetweenTimestamp(dto.getTimestampCreation(), credential.getTimestampCreation());
            testBetweenTimestamp(dto.getTimestampUpdated(), credential.getTimestampUpdated());
            assertEquals(dto.getSite(), credential.getSite());
            assertEquals(dto.getUsername(), credential.getUsername());
            assertEquals(dto.getPassword(), credential.getPassword());
        } else if (BASE_PATH_CARD.equals(url)) {
            var dto = fillObject(new CardDto());
            dto.setCvv("123");

            var card = given()
                    .contentType(ContentType.JSON)
                    .header(AUTH_HEADER_NAME, header)
                    .body(dto)
                    .post(url)
                    .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .extract()
                    .as(CardDto.class);


            assertNotNull(card.getId());
            assertEquals(dto.getName(), card.getName());
            assertEquals(dto.getNotes(), card.getNotes());
            testBetweenTimestamp(dto.getTimestampCreation(), card.getTimestampCreation());
            testBetweenTimestamp(dto.getTimestampUpdated(), card.getTimestampUpdated());
            assertEquals(dto.getCardHolder(), card.getCardHolder());
            assertEquals(dto.getNumber(), card.getNumber());
            assertEquals(dto.getExpiration(), card.getExpiration());
            assertEquals(dto.getCvv(), card.getCvv());
        } else {
            fail("Url not valid");
        }
    }

    private VaultDto getDtoFilled(String url) {
        if (url.equals(BASE_PATH_CREDENTIAL)) {
            return fillObject(new CredentialDto());
        }
        if (url.equals(BASE_PATH_CARD)) {
            return fillObject(new CardDto());
        }
        throw new IllegalArgumentException("URL not valid: " + url);
    }


}
