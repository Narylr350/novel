package com.wtl.novel.booksource.render;

import java.util.List;
import java.util.Map;

public record RenderedPage(
        String finalUrl,
        int statusCode,
        Map<String, List<String>> headers,
        String body,
        String rendererMode,
        String rendererProfile,
        String errorCode,
        String errorMessage
) {
}
