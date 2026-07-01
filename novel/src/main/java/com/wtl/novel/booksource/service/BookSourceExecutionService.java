package com.wtl.novel.booksource.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.booksource.entity.BookSourceEntity;
import com.wtl.novel.booksource.model.BookInfoRule;
import com.wtl.novel.booksource.model.BookSource;
import com.wtl.novel.booksource.model.ContentRule;
import com.wtl.novel.booksource.model.SearchRule;
import com.wtl.novel.booksource.model.SourceBook;
import com.wtl.novel.booksource.model.SourceChapter;
import com.wtl.novel.booksource.model.SourceContent;
import com.wtl.novel.booksource.render.RenderRequest;
import com.wtl.novel.booksource.render.RenderedPage;
import com.wtl.novel.booksource.render.RuleSourceRenderer;
import com.wtl.novel.booksource.model.TocRule;
import com.wtl.novel.booksource.repository.BookSourceRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BookSourceExecutionService {
    private static final int DEFAULT_TIMEOUT_MILLIS = 10000;

    private final BookSourceRepository repository;
    private final RuleSourceRenderer renderer;
    private final ObjectMapper objectMapper;

    public BookSourceExecutionService(
            BookSourceRepository repository,
            RuleSourceRenderer renderer,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.renderer = renderer;
        this.objectMapper = objectMapper;
    }

    public SourceSearchResult search(SearchRequest request) {
        LoadedSource loaded = loadSource(request.sourceId());
        SearchRule rule = loaded.source().getRuleSearch();
        if (rule == null || isBlank(rule.getBookList())) {
            return new SourceSearchResult(request.sourceId(), List.of());
        }
        Document document = fetchDocument(buildSearchUrl(loaded.source(), request.keyword(), request.page()));
        Elements bookElements = document.select(rule.getBookList());
        List<SourceBook> books = new ArrayList<>();
        for (Element element : bookElements) {
            String bookUrl = extract(element, rule.getBookUrl(), document.baseUri());
            books.add(new SourceBook(
                    request.sourceId(),
                    stableKey(request.sourceId(), bookUrl),
                    bookUrl,
                    extract(element, rule.getName(), document.baseUri()),
                    extract(element, rule.getAuthor(), document.baseUri()),
                    extract(element, rule.getKind(), document.baseUri()),
                    extract(element, rule.getCoverUrl(), document.baseUri()),
                    extract(element, rule.getIntro(), document.baseUri()),
                    extract(element, rule.getLastChapter(), document.baseUri()),
                    extract(element, rule.getWordCount(), document.baseUri()),
                    null,
                    null
            ));
        }
        return new SourceSearchResult(request.sourceId(), books);
    }

    public SourceBook detail(DetailRequest request) {
        LoadedSource loaded = loadSource(request.sourceId());
        BookInfoRule rule = loaded.source().getRuleBookInfo();
        Document document = fetchDocument(request.bookUrl());
        return new SourceBook(
                request.sourceId(),
                nonBlank(request.bookKey(), stableKey(request.sourceId(), request.bookUrl())),
                request.bookUrl(),
                extract(document, rule != null ? rule.getName() : null, document.baseUri()),
                extract(document, rule != null ? rule.getAuthor() : null, document.baseUri()),
                extract(document, rule != null ? rule.getKind() : null, document.baseUri()),
                extract(document, rule != null ? rule.getCoverUrl() : null, document.baseUri()),
                extract(document, rule != null ? rule.getIntro() : null, document.baseUri()),
                extract(document, rule != null ? rule.getLastChapter() : null, document.baseUri()),
                extract(document, rule != null ? rule.getWordCount() : null, document.baseUri()),
                extract(document, rule != null ? rule.getTocUrl() : null, document.baseUri()),
                extract(document, rule != null ? rule.getUpdateTime() : null, document.baseUri())
        );
    }

    public SourceTocResult toc(TocRequest request) {
        LoadedSource loaded = loadSource(request.sourceId());
        TocRule rule = loaded.source().getRuleToc();
        if (rule == null || isBlank(rule.getChapterList())) {
            return new SourceTocResult(request.sourceId(), request.bookKey(), List.of());
        }
        String tocUrl = nonBlank(request.tocUrl(), request.bookUrl());
        Document document = fetchDocument(tocUrl);
        Elements chapterElements = document.select(rule.getChapterList());
        List<SourceChapter> chapters = new ArrayList<>();
        for (int i = 0; i < chapterElements.size(); i++) {
            Element element = chapterElements.get(i);
            String chapterUrl = extract(element, rule.getChapterUrl(), document.baseUri());
            chapters.add(new SourceChapter(
                    stableKey(request.sourceId(), chapterUrl),
                    i,
                    extract(element, rule.getChapterName(), document.baseUri()),
                    chapterUrl,
                    parseBoolean(extract(element, rule.getIsVip(), document.baseUri())),
                    parseBoolean(extract(element, rule.getIsPay(), document.baseUri())),
                    parseBoolean(extract(element, rule.getIsVolume(), document.baseUri()))
            ));
        }
        return new SourceTocResult(request.sourceId(), request.bookKey(), chapters);
    }

    public SourceContent content(ContentRequest request) {
        LoadedSource loaded = loadSource(request.sourceId());
        ContentRule rule = loaded.source().getRuleContent();
        RenderedPage page = fetch(request.chapterUrl());
        Document document = Jsoup.parse(page.body(), page.finalUrl());
        return new SourceContent(
                request.sourceId(),
                request.bookKey(),
                request.chapterKey(),
                extract(document, rule != null ? rule.getTitle() : null, document.baseUri()),
                extract(document, rule != null ? rule.getContent() : null, document.baseUri()),
                page.rendererMode(),
                page.rendererProfile(),
                page.finalUrl()
        );
    }

    private LoadedSource loadSource(String sourceId) {
        BookSourceEntity entity = repository.findBySourceId(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("book source not found: " + sourceId));
        try {
            return new LoadedSource(objectMapper.readValue(entity.getRawJson(), BookSource.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("stored book source JSON cannot be parsed", e);
        }
    }

    private String buildSearchUrl(BookSource source, String keyword, int page) {
        if (isBlank(source.getSearchUrl())) {
            throw new IllegalArgumentException("searchUrl is required for source execution");
        }
        return source.getSearchUrl()
                .replace("{{key}}", encode(keyword))
                .replace("{{keyword}}", encode(keyword))
                .replace("{{page}}", Integer.toString(Math.max(page, 1)));
    }

    private Document fetchDocument(String url) {
        RenderedPage page = fetch(url);
        return Jsoup.parse(page.body(), page.finalUrl());
    }

    private RenderedPage fetch(String url) {
        RenderedPage page = renderer.fetch(new RenderRequest(
                url,
                "GET",
                Map.of(),
                null,
                StandardCharsets.UTF_8.name(),
                "http",
                "desktop",
                null,
                null,
                DEFAULT_TIMEOUT_MILLIS
        ));
        if (page.statusCode() >= 400) {
            throw new IllegalStateException("source fetch failed with HTTP " + page.statusCode());
        }
        return page;
    }

    private String extract(Element context, String rawRule, String baseUri) {
        String rule = stripRuleOptions(rawRule);
        if (isBlank(rule)) {
            return null;
        }
        // First execution slice: support CSS selector rules plus @text/@html/@href/@src extraction.
        int attrSeparator = rule.lastIndexOf('@');
        if (attrSeparator >= 0) {
            String selector = rule.substring(0, attrSeparator);
            String attribute = rule.substring(attrSeparator + 1);
            Element target = selector.isBlank() ? context : context.selectFirst(selector);
            return readAttribute(target, attribute);
        }
        Element target = context.selectFirst(rule);
        return target != null ? target.text() : null;
    }

    private String readAttribute(Element element, String attribute) {
        if (element == null || isBlank(attribute)) {
            return null;
        }
        return switch (attribute) {
            case "text" -> element.text();
            case "html" -> element.html();
            case "href", "src" -> {
                String absolute = element.absUrl(attribute);
                yield !absolute.isBlank() ? absolute : element.attr(attribute);
            }
            default -> element.attr(attribute);
        };
    }

    private String stripRuleOptions(String rawRule) {
        if (rawRule == null) {
            return null;
        }
        int optionsSeparator = rawRule.indexOf("##$##");
        if (optionsSeparator >= 0) {
            return rawRule.substring(0, optionsSeparator);
        }
        return rawRule;
    }

    private boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private String stableKey(String sourceId, String value) {
        if (isBlank(value)) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((sourceId + ":" + value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                builder.append(String.format("%02x", hash[i]));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    private String nonBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record LoadedSource(BookSource source) {
    }

    public record SearchRequest(String sourceId, String keyword, int page) {
    }

    public record DetailRequest(String sourceId, String bookKey, String bookUrl) {
    }

    public record TocRequest(String sourceId, String bookKey, String bookUrl, String tocUrl) {
    }

    public record ContentRequest(
            String sourceId,
            String bookKey,
            String chapterKey,
            String bookUrl,
            String tocUrl,
            String chapterUrl) {
    }

    public record SourceSearchResult(String sourceId, List<SourceBook> books) {
    }

    public record SourceTocResult(String sourceId, String bookKey, List<SourceChapter> chapters) {
    }

}
