package com.wtl.novel.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

import java.util.Arrays;
import java.util.List;

@Component
public class CorsPolicy {

    @Value("${app.cors.allowed-origin-patterns:}")
    private String allowedOriginPatternsRaw;

    public List<String> getAllowedOriginPatterns() {
        if (allowedOriginPatternsRaw == null || allowedOriginPatternsRaw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(allowedOriginPatternsRaw.split(","))
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty())
                .distinct()
                .toList();
    }

    public boolean hasConfiguredOrigins() {
        return !getAllowedOriginPatterns().isEmpty();
    }

    public boolean isOriginAllowed(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }
        return getAllowedOriginPatterns().stream()
                .anyMatch(pattern -> PatternMatchUtils.simpleMatch(pattern, origin));
    }

    public String resolveAllowedOrigin(String origin) {
        return isOriginAllowed(origin) ? origin : null;
    }
}
