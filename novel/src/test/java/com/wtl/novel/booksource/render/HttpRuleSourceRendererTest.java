package com.wtl.novel.booksource.render;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpRuleSourceRendererTest {

    @Test
    void fetchReadsHttpPageBodyAndStatus() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/book", exchange -> {
            byte[] body = "<html><body>Readable body</body></html>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/book";
            HttpRuleSourceRenderer renderer = new HttpRuleSourceRenderer();

            RenderedPage page = renderer.fetch(new RenderRequest(
                    url,
                    "GET",
                    Map.of(),
                    null,
                    "UTF-8",
                    "http",
                    "desktop",
                    null,
                    null,
                    3000
            ));

            assertEquals(200, page.statusCode());
            assertEquals("http", page.rendererMode());
            assertTrue(page.body().contains("Readable body"));
        } finally {
            server.stop(0);
        }
    }
}
