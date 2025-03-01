package com.ilmatty98.resource.vault;


import com.ilmatty98.AuthenticationServiceTests;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.ilmatty98.constants.UrlConstants.Card.BASE_PATH_CARD;
import static com.ilmatty98.constants.UrlConstants.Login.BASE_PATH_LOGIN;
import static io.restassured.RestAssured.given;

@QuarkusTest
class GetAllTest extends AuthenticationServiceTests {

    @ParameterizedTest
    @ValueSource(strings = {BASE_PATH_LOGIN, BASE_PATH_CARD})
    void testWithoutToken(String url) {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

}
