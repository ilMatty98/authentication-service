package com.ilmatty98.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotAuthorizedException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class TokenJwtService {

    @ConfigProperty(name = "token.expiration-minutes")
    long tokenExpiration;

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;

    private static final int KEY_SIZE = 2048;
    private static final String ALGORITHM = "RSA";

    @Startup
    void init() {
        generateKeyPair();
    }

    @Scheduled(cron = "{token.key-rotation.cron}")
    public static void generateKeyPair() {
        try {
            log.info("Started generation of key pair for jwt token");
            var keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);

            var keyPair = keyPairGenerator.generateKeyPair();
            publicKey = (RSAPublicKey) keyPair.getPublic();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            log.info("Finished generating key pair for jwt token");
        } catch (Exception e) {
            log.error("Error creating keys", e);
        }
    }

    public String generateTokenJwt(String subject, Map<String, Object> claims) {
        var now = Instant.now();
        claims.put(Claims.SUBJECT, subject);
        claims.put(Claims.ISSUED_AT, Date.from(now));
        claims.put(Claims.EXPIRATION, Date.from(now.plus(tokenExpiration, ChronoUnit.MINUTES)));

        return Jwts.builder()
                .claims(claims)
                .signWith(privateKey)
                .compact();
    }

    @SneakyThrows
    public String getPublicKey() {
        var keyFactory = KeyFactory.getInstance(ALGORITHM);
        var keySpec = keyFactory.getKeySpec(publicKey, X509EncodedKeySpec.class);
        var encodedKey = keySpec.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public Map<String, Object> validateTokenJwt(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token is expired");
            throw new NotAuthorizedException("");
        } catch (Exception e) {
            log.warn("Invalid token");
            throw new NotAuthorizedException("");
        }
    }

}
