package com.wtl.novel.Config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsPolicyTest {

    @Test
    void parsesConfiguredOriginsAndMatchesWildcardPatterns() {
        CorsPolicy policy = new CorsPolicy();
        ReflectionTestUtils.setField(policy, "allowedOriginPatternsRaw", "http://localhost:8080, https://*.example.com");

        assertEquals(List.of("http://localhost:8080", "https://*.example.com"), policy.getAllowedOriginPatterns());
        assertTrue(policy.isOriginAllowed("http://localhost:8080"));
        assertTrue(policy.isOriginAllowed("https://api.example.com"));
        assertFalse(policy.isOriginAllowed("https://evil.test"));
    }

    @Test
    void emptyConfigurationDisablesCrossOriginAccess() {
        CorsPolicy policy = new CorsPolicy();
        ReflectionTestUtils.setField(policy, "allowedOriginPatternsRaw", "   ");

        assertTrue(policy.getAllowedOriginPatterns().isEmpty());
        assertFalse(policy.isOriginAllowed("http://localhost:8080"));
    }
}
