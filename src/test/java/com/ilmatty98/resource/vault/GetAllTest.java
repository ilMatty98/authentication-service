package com.ilmatty98.resource.vault;


import com.ilmatty98.AuthenticationServiceTests;
import com.ilmatty98.constants.AccountStateEnum;
import com.ilmatty98.dto.vault.VaultDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.ilmatty98.constants.UrlConstants.Card.BASE_PATH_CARD;
import static com.ilmatty98.constants.UrlConstants.Credential.BASE_PATH_CREDENTIAL;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        var credentials = given()
                .contentType(ContentType.JSON)
                .when()
                .header(AUTH_HEADER_NAME, header)
                .get(url)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .as(VaultDto[].class);

        assertEquals(0, credentials.length);
    }


}
