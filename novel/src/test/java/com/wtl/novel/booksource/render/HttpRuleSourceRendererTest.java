package com.wtl.novel.booksource.render;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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

    @Test
    void fetchDecodesGzipEncodedPageBody() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/gzip", exchange -> {
            byte[] body = gzip("<html><body>Compressed readable body</body></html>");
            exchange.getResponseHeaders().add("Content-Encoding", "gzip");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/gzip";
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
            assertTrue(page.body().contains("Compressed readable body"));
        } finally {
            server.stop(0);
        }
    }

    private byte[] gzip(String value) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(value.getBytes(StandardCharsets.UTF_8));
        }
        return out.toByteArray();
    }
}
