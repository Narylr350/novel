package com.wtl.novel.booksource.render;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new RenderedPage(
                    response.uri().toString(),
                    response.statusCode(),
                    response.headers().map(),
                    response.body(),
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
}
