package com.ilmatty98;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusMain
public class Main {

    public static void main(String... args) {
        log.info("Running main method");
        Quarkus.run(args);
    }
}