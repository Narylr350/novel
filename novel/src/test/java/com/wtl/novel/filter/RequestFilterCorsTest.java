package com.wtl.novel.filter;

import com.wtl.novel.Config.CorsPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestFilterCorsTest {

    @Test
    void preflightAllowsConfiguredOriginAndEchoesRequestedHeaders() {
        RequestFilter filter = new RequestFilter();
        CorsPolicy corsPolicy = new CorsPolicy();
        ReflectionTestUtils.setField(corsPolicy, "allowedOriginPatternsRaw", "http://localhost:8080,http://localhost:8082");
        ReflectionTestUtils.setField(filter, "corsPolicy", corsPolicy);

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/novels/search");
        request.addHeader("Origin", "http://localhost:8082");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "Authorization, Content-Type");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean handled = ReflectionTestUtils.invokeMethod(filter, "handleOptionsRequest", request, response);

        assertEquals(true, handled);
        assertEquals(200, response.getStatus());
        assertEquals("http://localhost:8082", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("Authorization, Content-Type", response.getHeader("Access-Control-Allow-Headers"));
    }

    @Test
    void preflightRejectsDisallowedOrigin() {
        RequestFilter filter = new RequestFilter();
        CorsPolicy corsPolicy = new CorsPolicy();
        ReflectionTestUtils.setField(corsPolicy, "allowedOriginPatternsRaw", "http://localhost:8080");
        ReflectionTestUtils.setField(filter, "corsPolicy", corsPolicy);

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/novels/search");
        request.addHeader("Origin", "https://evil.test");
        request.addHeader("Access-Control-Request-Method", "GET");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean handled = ReflectionTestUtils.invokeMethod(filter, "handleOptionsRequest", request, response);

        assertEquals(true, handled);
        assertEquals(403, response.getStatus());
    }
}
