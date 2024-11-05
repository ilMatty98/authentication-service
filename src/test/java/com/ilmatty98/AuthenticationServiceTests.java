package com.ilmatty98;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilmatty98.constants.UserStateEnum;
import com.ilmatty98.dto.request.SignUpDto;
import com.ilmatty98.entity.User;
import com.ilmatty98.mapper.AuthenticationMapper;
import com.ilmatty98.repository.UserRepository;
import com.ilmatty98.service.EmailService;
import com.ilmatty98.service.TokenJwtService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AuthenticationServiceTests extends ApiTestConstants {
    @Inject
    protected EmailService emailService;

    @Inject
    protected TokenJwtService tokenJwtService;

    @Inject
    protected UserRepository userRepository;

    @Inject
    protected AuthenticationMapper authenticationMapper;

    @ConfigProperty(name = "token.expiration-minutes")
    protected long tokenExpiration;

    private static final Random random = new Random();

    @BeforeEach
    @Transactional
    public void cleanRepository() {
        userRepository.deleteAll();
    }

    protected static String generateRandomString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    @SneakyThrows
    protected static <T> T fillObject(T object) {
        var superclass = object.getClass().getSuperclass();
        if (superclass != null && !superclass.equals(Object.class))
            fillObject(object, superclass);

        fillObject(object, object.getClass());
        return object;
    }

    @SneakyThrows
    private static void fillObject(Object object, Class<?> clazz) {
        var fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() == int.class) {
                field.setInt(object, random.nextInt());
            } else if (field.getType() == long.class) {
                field.setLong(object, random.nextLong());
            } else if (field.getType() == double.class) {
                field.setDouble(object, random.nextDouble());
            } else if (field.getType() == float.class) {
                field.setFloat(object, random.nextFloat());
            } else if (field.getType() == boolean.class) {
                field.setBoolean(object, random.nextBoolean());
            } else if (field.getType() == char.class) {
                field.setChar(object, (char) (random.nextInt(26) + 'a'));
            } else if (field.getType() == String.class) {
                field.set(object, generateRandomString(random.nextInt(20) + 1));
            } else if (field.getType() == Timestamp.class) {
                field.set(object, Timestamp.from(Instant.now()));
            } else if (field.getType() == BigInteger.class) {
                field.set(object, BigInteger.valueOf(random.nextLong(1000)));
            } else if (field.getType() == UserStateEnum.class) {
                field.set(object, UserStateEnum.VERIFIED);
            }
        }
    }

    protected static String objectToJsonString(Object object) throws JsonProcessingException {
        var ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(object);
    }

    protected User signUp(String email, String password) throws Exception {
        var signUp = new SignUpDto();
        signUp.setEmail(email);
        signUp.setMasterPasswordHash(password);
        signUp.setInitializationVector("initVector");
        signUp.setProtectedSymmetricKey("protectedSymmetricKey");
        signUp.setLanguage(EN);
        signUp.setHint("Hint");
        signUp.setPropic("Propic");

        given()
                .contentType("application/json")
                .body(signUp)
                .when()
                .post(SIGN_UP_URL)
                .then()
                .statusCode(204);

        return userRepository.findByEmail(email).orElseGet(Assertions::fail);
    }


    protected static LocalDateTime getLocalDataTime(Timestamp timestamp) {
        return Optional.ofNullable(timestamp)
                .map(Timestamp::toLocalDateTime)
                .map(l -> l.truncatedTo(ChronoUnit.SECONDS))
                .orElse(null);
    }

    protected static String createLargeString(double mb) {
        var desiredSizeInBytes = mb * 1024 * 1024; // 3 MB
        var chunkSize = 1024; // Chunk size for each iteration
        var chunkCount = desiredSizeInBytes / chunkSize;

        var sb = new StringBuilder();

        // Generate a chunk of characters to fill the StringBuilder
        var chunk = new String(new char[chunkSize]).replace("\0", "x");

        sb.append(chunk.repeat((int) chunkCount));

        return sb.toString();
    }
}
