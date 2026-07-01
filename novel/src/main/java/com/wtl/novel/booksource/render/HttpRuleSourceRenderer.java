package com.wtl.novel.booksource.render;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

@Component
public class HttpRuleSourceRenderer implements RuleSourceRenderer {
    private static final int DEFAULT_TIMEOUT_MILLIS = 10000;

    private final HttpClient client;

    public HttpRuleSourceRenderer() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(DEFAULT_TIMEOUT_MILLIS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    HttpRuleSourceRenderer(HttpClient client) {
        this.client = client;
    }

    @Override
    public RenderedPage fetch(RenderRequest request) {
        URI uri = URI.create(request.url());
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Only http and https source URLs are supported");
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMillis(timeoutMillis(request)))
                .GET();
        request.headers().forEach(builder::header);
        try {
            HttpResponse<byte[]> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            return new RenderedPage(
                    response.uri().toString(),
                    response.statusCode(),
                    response.headers().map(),
                    decodeBody(response.body(), response.headers().map(), request),
                    "http",
                    "desktop",
                    null,
                    null
            );
        } catch (IOException e) {
            throw new IllegalStateException("HTTP source fetch failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("HTTP source fetch interrupted", e);
        }
    }

    private int timeoutMillis(RenderRequest request) {
        return request.timeoutMillis() > 0 ? request.timeoutMillis() : DEFAULT_TIMEOUT_MILLIS;
    }

    private String decodeBody(byte[] body, Map<String, List<String>> headers, RenderRequest request) throws IOException {
        byte[] decoded = switch (firstHeader(headers, "content-encoding").toLowerCase(Locale.ROOT)) {
            case "gzip" -> new GZIPInputStream(new ByteArrayInputStream(body)).readAllBytes();
            case "deflate" -> new InflaterInputStream(new ByteArrayInputStream(body)).readAllBytes();
            default -> body;
        };
        return new String(decoded, charset(request));
    }

    private String firstHeader(Map<String, List<String>> headers, String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .flatMap(entry -> entry.getValue().stream())
                .findFirst()
                .orElse("");
    }

    private Charset charset(RenderRequest request) {
        if (request.charset() == null || request.charset().isBlank()) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName(request.charset());
    }
}
