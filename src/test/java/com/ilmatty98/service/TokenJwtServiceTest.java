package com.ilmatty98.service;

import com.ilmatty98.AuthenticationServiceTests;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TokenJwtServiceTest extends AuthenticationServiceTests {

    @Test
    void testGenerateTokenJwtAndValidate() {
        var subject = "Subject";
        var claims = new HashMap<String, Object>();
        claims.put("claim1", generateRandomString(10));
        claims.put("claim2", generateRandomString(100));

        var token = tokenJwtService.generateTokenJwt(claims);
        assertNotNull(token);

        var claimsFromToken = tokenJwtService.validateTokenJwt(token);
        assertEquals(claims.get("claim1"), claimsFromToken.get("claim1"));
        assertEquals(claims.get("claim2"), claimsFromToken.get("claim2"));
    }

    @Test
    void testGetPublicKey() {
        assertNotNull(tokenJwtService.getPublicKey());
    }

    @Test
    @Disabled
    void testValidateTokenJwtExpired() throws InterruptedException {
        var now = Instant.now();
        var subject = "Subject";
        var claims = new HashMap<String, Object>();
        claims.put("claim1", generateRandomString(10));
        claims.put("claim2", generateRandomString(100));
        claims.put(Claims.SUBJECT, subject);
        claims.put(Claims.ISSUED_AT, Date.from(now));
        claims.put(Claims.EXPIRATION, Date.from(now.plus(1, ChronoUnit.SECONDS)));


        // Generates a token that expires in 1 second
        var expiredToken = Jwts.builder()
                .claims(claims)
//                .signWith(tokenJwtService.privateKey)
                .compact();

        Thread.sleep(1000);

        assertThrows(UnauthorizedException.class, () -> {
            tokenJwtService.validateTokenJwt(expiredToken);
        });
    }


}
