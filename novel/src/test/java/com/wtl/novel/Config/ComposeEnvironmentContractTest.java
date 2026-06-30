package com.wtl.novel.Config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ComposeEnvironmentContractTest {

    @Test
    void composeEntrypointsPassDocumentedCorsEnvironmentVariable() throws IOException {
        assertComposePassesCorsVariable(Path.of("../docker-compose.local-single.yml"));
        assertComposePassesCorsVariable(Path.of("../docker-compose.local-dual.yml"));
        assertComposePassesCorsVariable(Path.of("../docker-compose.external-single.yml"));
        assertComposePassesCorsVariable(Path.of("../docker-compose.external-dual.yml"));
    }

    private static void assertComposePassesCorsVariable(Path path) throws IOException {
        String content = Files.readString(path);
        assertTrue(content.contains("APP_CORS_ALLOWED_ORIGIN_PATTERNS: ${APP_CORS_ALLOWED_ORIGIN_PATTERNS:-}"),
                path + " should pass the documented CORS environment variable into the backend container");
    }
}
