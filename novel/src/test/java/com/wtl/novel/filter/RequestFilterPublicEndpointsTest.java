package com.wtl.novel.filter;

import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.RequestLogService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class RequestFilterPublicEndpointsTest {

    @Test
    void publicBootstrapEndpointsBypassAuthWithoutToken() throws Exception {
        assertPublicGet("/api/auth/app-mode");
        assertPublicGet("/api/auth/isLogin");
        assertPublicGet("/api/platform/novel");
        assertPublicGet("/api/dic/getHome");
        assertPublicGet("/api/tag/all/novelPia");
        assertPublicGet("/api/tag/all/novelPia/校园");
        assertPublicGet("/api/novels/1");
        assertPublicGet("/api/chapters/getChaptersByNovelId/1");
        assertPublicGet("/api/tag/getTagsAllInfoByNovelId/1");
        assertPublicGet("/api/posts/getAllPostsByNovelId");
        assertPublicGet("/api/favorites/user/1/novelPia");
        assertPublicPost("/api/novels/getNovelsByPlatform");
    }

    @Test
    void protectedApiStillRejectsMissingToken() throws Exception {
        RequestFilter filter = createFilter();
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/msg/getMessage");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        verifyNoInteractions(chain);
    }

    private void assertPublicGet(String uri) throws Exception {
        RequestFilter filter = createFilter();
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus(), uri + " should not be blocked by auth filter");
        verify(chain).doFilter(request, response);
    }

    private void assertPublicPost(String uri) throws Exception {
        RequestFilter filter = createFilter();
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus(), uri + " should not be blocked by auth filter");
        verify(chain).doFilter(request, response);
    }

    private RequestFilter createFilter() {
        RequestFilter filter = new RequestFilter();
        ReflectionTestUtils.setField(filter, "credentialService", mock(CredentialService.class));
        ReflectionTestUtils.setField(filter, "requestLogService", mock(RequestLogService.class));
        return filter;
    }
}
