package com.ilmatty98.utils;

import lombok.experimental.UtilityClass;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@UtilityClass
public class AuthenticationUtils {

    public static byte[] generateSalt(int saltSize) {
        var salt = new byte[saltSize];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static byte[] generateArgon2id(String password, byte[] salt, int argon2idSize, int iteration, int memLimitKB, int parallelism) {
        var generator = new Argon2BytesGenerator();
        var aragon2id = new byte[argon2idSize];
        var builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(iteration)
                .withMemoryAsKB(memLimitKB)
                .withParallelism(parallelism)
                .withSalt(salt);

        generator.init(builder.build());
        generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), aragon2id, 0, aragon2id.length);
        return aragon2id;
    }
}
