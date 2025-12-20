package com.wtl.novel.filter;

import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.RequestLogService;
import com.wtl.novel.entity.Credential;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import static com.wtl.novel.util.URLMatcher.matches;

@Component
public class RequestFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestFilter.class);

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private RequestLogService requestLogService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (handleOptionsRequest(httpRequest, httpResponse)) {
                return;
            }

            String requestURI = httpRequest.getRequestURI();

            if (handleActuatorRequest(httpRequest, httpResponse, requestURI)) {
                return;
            }

            if (!requestURI.startsWith("/api/")) {
                chain.doFilter(request, response);
                return;
            }

            if (matches(requestURI)) {
                chain.doFilter(request, response);
                return;
            }

            // 统一从请求中获取 token（支持 Header 和 URL 参数两种方式）
            String token = getTokenFromRequest(httpRequest);
            if (token == null || token.isEmpty()) {
                httpResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "缺少认证信息");
                return;
            }

            Credential credential = credentialService.findByToken(token);
            if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
                httpResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "无效的凭据");
                return;
            }

            if (!requestLogService.checkRequestLimit(credential, requestURI)) {
                httpResponse.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "请求次数已用尽");
                return;
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("请求过滤器处理异常", e);
        }
    }

    /**
     * 从请求中获取 token
     * 1. 优先从 Authorization header 获取
     * 2. 其次从 URL 参数 token 获取（用于浏览器直接下载场景）
     */
    private String getTokenFromRequest(HttpServletRequest httpRequest) {
        // 1. 先从 Authorization header 获取
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            // 支持 "token" 或 "token;other" 格式
            String[] parts = authHeader.split(";");
            if (parts.length > 0 && parts[0] != null && !parts[0].trim().isEmpty()) {
                return parts[0].trim();
            }
        }
        
        // 2. 从 URL 参数获取 token（用于浏览器直接下载）
        String tokenParam = httpRequest.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam.trim();
        }
        
        return null;
    }

    private boolean handleOptionsRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (!httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            return false;
        }

        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        return true;
    }

    private boolean handleActuatorRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String requestURI) {
        if (!requestURI.startsWith("/actuator")) {
            return false;
        }

        String remoteAddr = httpRequest.getRemoteAddr();
        
        if (remoteAddr != null && (remoteAddr.equals("127.0.0.1") ||
                remoteAddr.equals("0:0:0:0:0:0:0:1") ||
                remoteAddr.equals("::1") ||
                remoteAddr.equals("localhost"))) {
            return false; // 返回false表示应该继续处理请求
        } else {
            try {
                httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                httpResponse.setContentType("text/html;charset=UTF-8");
                httpResponse.getWriter().write("");
                httpResponse.getWriter().flush();
            } catch (Exception e) {
                log.error("写入响应失败", e);
            }
            return true; // 返回true表示已经处理了请求
        }
    }

}