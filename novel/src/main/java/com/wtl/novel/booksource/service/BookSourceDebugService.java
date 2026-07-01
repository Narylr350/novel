package com.wtl.novel.booksource.service;

import com.wtl.novel.booksource.model.SourceBook;
import com.wtl.novel.booksource.model.SourceChapter;
import com.wtl.novel.booksource.model.SourceContent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookSourceDebugService {
    private static final int CONTENT_PREVIEW_LIMIT = 200;

    private final BookSourceExecutionService executionService;

    public BookSourceDebugService(BookSourceExecutionService executionService) {
        this.executionService = executionService;
    }

    public DebugRunResult run(DebugRunRequest request) {
        List<DebugStep> steps = new ArrayList<>();
        try {
            BookSourceExecutionService.SourceSearchResult search = executionService.search(
                    new BookSourceExecutionService.SearchRequest(request.sourceId(), request.keyword(), 1));
            if (search.books().isEmpty() && isBlank(request.bookUrl())) {
                return failed(steps, request.rendererMode(), "search", "SEARCH_EMPTY", "search returned no books");
            }
            steps.add(success("search", request.rendererMode()));

            SourceBook firstBook = firstBook(request, search);
            SourceBook detail = executionService.detail(new BookSourceExecutionService.DetailRequest(
                    request.sourceId(), firstBook.bookKey(), firstBook.bookUrl()));
            steps.add(success("detail", request.rendererMode()));

            BookSourceExecutionService.SourceTocResult toc = executionService.toc(
                    new BookSourceExecutionService.TocRequest(
                            request.sourceId(), firstBook.bookKey(), firstBook.bookUrl(), detail.tocUrl()));
            if (toc.chapters().isEmpty()) {
                return failed(steps, request.rendererMode(), "toc", "TOC_EMPTY", "toc returned no chapters");
            }
            steps.add(success("toc", request.rendererMode()));

            SourceChapter firstChapter = toc.chapters().getFirst();
            SourceContent content = executionService.content(new BookSourceExecutionService.ContentRequest(
                    request.sourceId(),
                    firstBook.bookKey(),
                    firstChapter.chapterKey(),
                    firstBook.bookUrl(),
                    detail.tocUrl(),
                    firstChapter.chapterUrl()));
            steps.add(success("content", content.rendererMode()));

            return new DebugRunResult(
                    true,
                    "passed",
                    new DebugSummary(
                            search.books().size(),
                            firstBook.name(),
                            toc.chapters().size(),
                            preview(content.content())),
                    steps
            );
        } catch (RuntimeException e) {
            steps.add(new DebugStep("run", "failed", rendererMode(request.rendererMode()), "EXECUTION_ERROR", e.getMessage()));
            return new DebugRunResult(false, "failed", new DebugSummary(0, null, 0, null), steps);
        }
    }

    private SourceBook firstBook(DebugRunRequest request, BookSourceExecutionService.SourceSearchResult search) {
        if (!isBlank(request.bookUrl())) {
            return new SourceBook(request.sourceId(), "", request.bookUrl(), null, null, null, null, null, null, null, null, null);
        }
        return search.books().getFirst();
    }

    private DebugRunResult failed(
            List<DebugStep> steps,
            String requestedRendererMode,
            String phase,
            String errorCode,
            String errorMessage) {
        steps.add(new DebugStep(phase, "failed", rendererMode(requestedRendererMode), errorCode, errorMessage));
        return new DebugRunResult(false, "failed", new DebugSummary(0, null, 0, null), steps);
    }

    private DebugStep success(String phase, String requestedRendererMode) {
        return new DebugStep(phase, "success", rendererMode(requestedRendererMode), null, null);
    }

    private String preview(String content) {
        if (content == null || content.length() <= CONTENT_PREVIEW_LIMIT) {
            return content;
        }
        return content.substring(0, CONTENT_PREVIEW_LIMIT);
    }

    private String nonBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String rendererMode(String requestedRendererMode) {
        return nonBlank(requestedRendererMode, "http");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record DebugRunRequest(String sourceId, String keyword, String bookUrl, String rendererMode) {
    }

    public record DebugRunResult(boolean ok, String finalStatus, DebugSummary summary, List<DebugStep> steps) {
    }

    public record DebugSummary(int resultCount, String firstBook, int chapterCount, String contentPreview) {
    }

    public record DebugStep(String phase, String status, String rendererMode, String errorCode, String errorMessage) {
    }
}
