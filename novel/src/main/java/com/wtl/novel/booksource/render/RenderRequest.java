package com.wtl.novel.booksource.render;

import java.util.Map;

public record RenderRequest(
        String url,
        String method,
        Map<String, String> headers,
        String body,
        String charset,
        String rendererMode,
        String rendererProfile,
        String webJs,
        String sourceRegex,
        int timeoutMillis
) {
}
