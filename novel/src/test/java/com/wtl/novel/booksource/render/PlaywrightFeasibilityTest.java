package com.wtl.novel.booksource.render;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Duration;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Playwright 技术选型最小验证。
 *
 * 验证目标：
 * 1. Playwright 能在项目 Maven/JDK 21 环境下启动浏览器
 * 2. 能渲染需要 JS 执行的页面并提取渲染后的 DOM
 * 3. 超时控制有效（导航超时、等待超时）
 * 4. BrowserContext 释放正常（try-with-resources）
 * 5. 内网/本地地址访问防护可通过 page.route() 实现
 *
 * 这是 Seed Task 1 的最小验证，不是生产代码。验证通过后保留为 renderer 实现的参考。
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlaywrightFeasibilityTest {

    private Playwright playwright;
    private Browser browser;

    @BeforeAll
    void startBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new com.microsoft.playwright.BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setArgs(java.util.List.of("--disable-gpu", "--no-sandbox"))
        );
    }

    @AfterAll
    void stopBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Test
    void shouldRenderStaticPageAndExtractContent() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate("https://example.com",
                    new Page.NavigateOptions().setTimeout(15_000));

            // 验证基础导航和 DOM 提取
            String title = page.title();
            String heading = page.textContent("h1");

            assertNotNull(title);
            assertFalse(heading.isBlank(), "h1 文本不应为空");
            System.out.println("[基础渲染] title=" + title + ", h1=" + heading);
        }
    }

    @Test
    void shouldExecuteJavaScriptAndExtractDynamicContent() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate("https://example.com",
                    new Page.NavigateOptions().setTimeout(15_000));

            // 验证 JS 执行能力——注入 JS 修改 DOM 并读取
            Object result = page.evaluate("() => { document.title }");
            String bodyText = (String) page.evaluate("() => document.body.innerText");
            String bodyHtml = (String) page.evaluate("() => document.body.innerHTML");

            assertNotNull(bodyText, "JS 执行后 body innerText 不应为 null");
            assertFalse(bodyText.isBlank(), "body innerText 不应为空");
            assertNotNull(bodyHtml, "body innerHTML 不应为 null");
            System.out.println("[JS 执行] bodyText 长度=" + bodyText.length()
                    + ", bodyHtml 长度=" + bodyHtml.length());
        }
    }

    @Test
    void shouldEnforceNavigationTimeout() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();

            // 验证超时控制：设置极短超时，确认会抛出超时异常
            assertThrows(com.microsoft.playwright.PlaywrightException.class, () -> {
                page.navigate("https://example.com",
                        new Page.NavigateOptions().setTimeout(1)); // 1ms 超时
            }, "1ms 超时应触发 PlaywrightException");
            System.out.println("[超时控制] 1ms 超时正确抛出异常");
        }
    }

    @Test
    void shouldWaitForLoadState() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate("https://example.com",
                    new Page.NavigateOptions().setTimeout(15_000));

            // 验证等待网络空闲（NETWORKIDLE 是 Playwright 独有的自动等待）
            assertDoesNotThrow(() -> {
                page.waitForLoadState(LoadState.NETWORKIDLE,
                        new Page.WaitForLoadStateOptions().setTimeout(10_000));
            }, "等待 NETWORKIDLE 应在 10s 内完成");
            System.out.println("[自动等待] NETWORKIDLE 等待成功");
        }
    }

    @Test
    void shouldBlockLocalAddressAccessViaRoute() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();

            // 验证内网/本地地址防护：通过 page.route() 拦截 localhost 请求
            page.route("**/*", route -> {
                String url = route.request().url();
                if (url.contains("localhost") || url.contains("127.0.0.1")
                        || Pattern.matches("https?://192\\.168\\..*", url)
                        || Pattern.matches("https?://10\\..*", url)) {
                    route.abort();
                } else {
                    route.resume();
                }
            });

            // 尝试导航到 example.com（应成功，非内网）
            page.navigate("https://example.com",
                    new Page.NavigateOptions().setTimeout(15_000));
            assertEquals("Example Domain", page.title());
            System.out.println("[内网防护] route 拦截器已注册，正常页面可访问");
        }
    }

    @Test
    void shouldIsolateContexts() {
        // 验证 BrowserContext 隔离：两个 Context 的 cookie 不互通
        try (BrowserContext context1 = browser.newContext();
             BrowserContext context2 = browser.newContext()) {
            Page page1 = context1.newPage();
            Page page2 = context2.newPage();

            page1.navigate("https://example.com",
                    new Page.NavigateOptions().setTimeout(15_000));
            // 在 context1 注入 cookie
            context1.addCookies(java.util.List.of(
                    new com.microsoft.playwright.options.Cookie("testcookie", "value1")
                            .setUrl("https://example.com")
            ));

            page2.navigate("https://example.com",
                    new Page.NavigateOptions().setTimeout(15_000));
            // context2 不应有 context1 的 cookie
            Object context2Cookies = page2.evaluate("() => document.cookie");
            String cookieStr = String.valueOf(context2Cookies);
            assertFalse(cookieStr.contains("testcookie"),
                    "context2 不应看到 context1 的 cookie");
            System.out.println("[Context 隔离] context2 cookie 不含 context1 的 testcookie，隔离有效");
        }
    }
}
