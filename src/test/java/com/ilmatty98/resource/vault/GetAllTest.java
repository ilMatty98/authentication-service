package com.ilmatty98.resource.vault;


import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.vault.CardDto;
import com.ilmatty98.dto.vault.CredentialDto;
import com.ilmatty98.dto.vault.VaultDto;
import com.ilmatty98.entity.Card;
import com.ilmatty98.entity.Credential;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.ilmatty98.constants.UrlConstants.Card.BASE_PATH_CARD;
import static com.ilmatty98.constants.UrlConstants.Credential.BASE_PATH_CREDENTIAL;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
class GetAllTest extends AuthenticationServiceTests {

    @ParameterizedTest
    @ValueSource(strings = {BASE_PATH_CREDENTIAL, BASE_PATH_CARD})
    void testWithoutToken(String url) {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
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
                .get(url)
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
                .get(url)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {BASE_PATH_CREDENTIAL, BASE_PATH_CARD})
    void testGetAllEmpty(String url) {
        signUp(EMAIL, PASSWORD);
        confirmEmail(EMAIL);
        var header = AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD);

        var vaults = given()
                .contentType(ContentType.JSON)
                .when()
                .header(AUTH_HEADER_NAME, header)
                .get(url)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .as(VaultDto[].class);

        assertEquals(0, vaults.length);
    }

    @ParameterizedTest
    @ValueSource(strings = {BASE_PATH_CREDENTIAL, BASE_PATH_CARD})
    void testGetAll(String url) {
        var account = signUp(EMAIL, PASSWORD);
        account = confirmEmail(EMAIL);
        var header = AUTH_HEADER_PREFIX + getTokenFromLogIn(EMAIL, PASSWORD);

        var card1 = fillObject(new Card());
        card1.setAccount(account);
        card1 = saveVault(card1);

        var card2 = fillObject(new Card());
        card2.setAccount(account);
        card2 = saveVault(card2);

        var credential1 = fillObject(new Credential());
        credential1.setAccount(account);
        credential1 = saveVault(credential1);

        var credential2 = fillObject(new Credential());
        credential2.setAccount(account);
        credential2 = saveVault(credential2);

        if (BASE_PATH_CREDENTIAL.equals(url)) {
            var credentials = given()
                    .contentType(ContentType.JSON)
                    .header(AUTH_HEADER_NAME, header)
                    .get(url)
                    .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .extract()
                    .as(CredentialDto[].class);

            assertEquals(2, credentials.length);

            assertEquals(credential1.getId(), credentials[0].getId());
            assertEquals(credential1.getName(), credentials[0].getName());
            assertEquals(credential1.getNotes(), credentials[0].getNotes());
            assertEquals(credential1.getTimestampCreation(), credentials[0].getTimestampCreation());
            assertEquals(credential1.getTimestampUpdated(), credentials[0].getTimestampUpdated());
            assertEquals(credential1.getSite(), credentials[0].getSite());
            assertEquals(credential1.getUsername(), credentials[0].getUsername());
            assertEquals(credential1.getPassword(), credentials[0].getPassword());

            assertEquals(credential2.getId(), credentials[1].getId());
            assertEquals(credential2.getName(), credentials[1].getName());
            assertEquals(credential2.getNotes(), credentials[1].getNotes());
            assertEquals(credential2.getTimestampCreation(), credentials[1].getTimestampCreation());
            assertEquals(credential2.getTimestampUpdated(), credentials[1].getTimestampUpdated());
            assertEquals(credential2.getSite(), credentials[1].getSite());
            assertEquals(credential2.getUsername(), credentials[1].getUsername());
            assertEquals(credential2.getPassword(), credentials[1].getPassword());
        } else if (BASE_PATH_CARD.equals(url)) {
            var cards = given()
                    .contentType(ContentType.JSON)
                    .header(AUTH_HEADER_NAME, header)
                    .get(url)
                    .then()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .extract()
                    .as(CardDto[].class);

            assertEquals(2, cards.length);

            assertEquals(card1.getId(), cards[0].getId());
            assertEquals(card1.getName(), cards[0].getName());
            assertEquals(card1.getNotes(), cards[0].getNotes());
            assertEquals(card1.getTimestampCreation(), cards[0].getTimestampCreation());
            assertEquals(card1.getTimestampUpdated(), cards[0].getTimestampUpdated());
            assertEquals(card1.getCardHolder(), cards[0].getCardHolder());
            assertEquals(card1.getNumber(), cards[0].getNumber());
            assertEquals(card1.getExpiration(), cards[0].getExpiration());
            assertEquals(card1.getCvv(), cards[0].getCvv());

            assertEquals(card2.getId(), cards[1].getId());
            assertEquals(card2.getName(), cards[1].getName());
            assertEquals(card2.getNotes(), cards[1].getNotes());
            assertEquals(card2.getTimestampCreation(), cards[1].getTimestampCreation());
            assertEquals(card2.getTimestampUpdated(), cards[1].getTimestampUpdated());
            assertEquals(card2.getCardHolder(), cards[1].getCardHolder());
            assertEquals(card2.getNumber(), cards[1].getNumber());
            assertEquals(card2.getExpiration(), cards[1].getExpiration());
            assertEquals(card2.getCvv(), cards[1].getCvv());
        } else {
            fail("Url not valid");
        }
    }


}
