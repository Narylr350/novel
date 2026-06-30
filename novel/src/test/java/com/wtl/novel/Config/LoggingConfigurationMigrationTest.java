package com.wtl.novel.Config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LoggingConfigurationMigrationTest {

    @Test
    void runtimeLoggingConfigurationUsesSpringBoot4RollingPolicyProperties() throws IOException {
        assertBoot4LoggingProperties(Path.of("src/main/resources/application-dev.properties"));
        assertBoot4LoggingProperties(Path.of("src/main/resources/application-prod.properties"));

        String pom = Files.readString(Path.of("pom.xml"));
        assertFalse(pom.contains("spring-boot-properties-migrator"));
    }

    private static void assertBoot4LoggingProperties(Path path) throws IOException {
        String content = Files.readString(path);
        assertFalse(content.contains("logging.file.max-size"), path + " should not use the old max-size property");
        assertFalse(content.contains("logging.file.max-history"), path + " should not use the old max-history property");
        assertTrue(content.contains("logging.logback.rollingpolicy.max-file-size=10MB"),
                path + " should configure Logback max file size with the Spring Boot 4 property");
        assertTrue(content.contains("logging.logback.rollingpolicy.max-history=30"),
                path + " should configure Logback max history with the Spring Boot 4 property");
    }
}
