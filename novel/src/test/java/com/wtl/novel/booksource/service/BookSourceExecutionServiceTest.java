package com.wtl.novel.booksource.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.booksource.entity.BookSourceEntity;
import com.wtl.novel.booksource.model.SourceBook;
import com.wtl.novel.booksource.model.SourceChapter;
import com.wtl.novel.booksource.model.SourceContent;
import com.wtl.novel.booksource.render.RenderRequest;
import com.wtl.novel.booksource.render.RenderedPage;
import com.wtl.novel.booksource.render.RuleSourceRenderer;
import com.wtl.novel.booksource.repository.BookSourceRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookSourceExecutionServiceTest {

    @Test
    void httpOnlyChainExtractsSearchDetailTocAndContent() {
        BookSourceRepository repository = mock(BookSourceRepository.class);
        BookSourceEntity entity = new BookSourceEntity();
        entity.setSourceId("source-1");
        entity.setBookSourceName("Example");
        entity.setBookSourceUrl("http://example.test");
        entity.setRawJson("""
                {
                  "bookSourceUrl": "http://example.test",
                  "bookSourceName": "Example",
                  "searchUrl": "http://example.test/search?q={{key}}&page={{page}}",
                  "ruleSearch": {
                    "bookList": ".book",
                    "name": ".title@text",
                    "author": ".author@text",
                    "bookUrl": ".title@href"
                  },
                  "ruleBookInfo": {
                    "name": "h1@text",
                    "author": ".author@text",
                    "intro": ".intro@text",
                    "tocUrl": ".toc@href"
                  },
                  "ruleToc": {
                    "chapterList": ".chapter",
                    "chapterName": "text",
                    "chapterUrl": "@href"
                  },
                  "ruleContent": {
                    "title": "h1@text",
                    "content": ".content@text"
                  }
                }
                """);
        when(repository.findBySourceId("source-1")).thenReturn(Optional.of(entity));
        BookSourceExecutionService service = new BookSourceExecutionService(
                repository,
                new FixtureRenderer(Map.of(
                        "http://example.test/search?q=test&page=1", """
                                <div class="book">
                                  <a class="title" href="/book/1">Test Book</a>
                                  <span class="author">Alice</span>
                                </div>
                                """,
                        "http://example.test/book/1", """
                                <h1>Test Book</h1>
                                <span class="author">Alice</span>
                                <p class="intro">Intro text</p>
                                <a class="toc" href="/book/1/toc">Catalog</a>
                                """,
                        "http://example.test/book/1/toc", """
                                <a class="chapter" href="/book/1/1">Chapter One</a>
                                <a class="chapter" href="/book/1/2">Chapter Two</a>
                                """,
                        "http://example.test/book/1/1", """
                                <h1>Chapter One</h1>
                                <div class="content">Readable chapter text</div>
                                """
                )),
                new ObjectMapper()
        );

        BookSourceExecutionService.SourceSearchResult search = service.search(
                new BookSourceExecutionService.SearchRequest("source-1", "test", 1));
        SourceBook firstBook = search.books().getFirst();
        assertEquals("Test Book", firstBook.name());
        assertEquals("Alice", firstBook.author());
        assertEquals("http://example.test/book/1", firstBook.bookUrl());
        assertFalse(firstBook.bookKey().isBlank());

        SourceBook detail = service.detail(
                new BookSourceExecutionService.DetailRequest("source-1", firstBook.bookKey(), firstBook.bookUrl()));
        assertEquals("Intro text", detail.intro());
        assertEquals("http://example.test/book/1/toc", detail.tocUrl());

        BookSourceExecutionService.SourceTocResult toc = service.toc(
                new BookSourceExecutionService.TocRequest("source-1", firstBook.bookKey(), firstBook.bookUrl(), detail.tocUrl()));
        List<SourceChapter> chapters = toc.chapters();
        assertEquals(2, chapters.size());
        assertEquals("Chapter One", chapters.getFirst().title());
        assertEquals("http://example.test/book/1/1", chapters.getFirst().chapterUrl());

        SourceContent content = service.content(
                new BookSourceExecutionService.ContentRequest(
                        "source-1",
                        firstBook.bookKey(),
                        chapters.getFirst().chapterKey(),
                        firstBook.bookUrl(),
                        detail.tocUrl(),
                        chapters.getFirst().chapterUrl()));
        assertEquals("Chapter One", content.title());
        assertEquals("Readable chapter text", content.content());
        assertEquals("http", content.rendererMode());
    }

    private record FixtureRenderer(Map<String, String> pages) implements RuleSourceRenderer {
        @Override
        public RenderedPage fetch(RenderRequest request) {
            String body = pages.get(request.url());
            if (body == null) {
                throw new IllegalArgumentException("No fixture for " + request.url());
            }
            return new RenderedPage(request.url(), 200, Map.of(), body, "http", "desktop", null, null);
        }
    }
}
