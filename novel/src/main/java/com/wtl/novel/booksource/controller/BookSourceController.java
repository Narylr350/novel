package com.wtl.novel.booksource.controller;

import com.wtl.novel.booksource.service.BookSourceImportService;
import com.wtl.novel.booksource.service.BookSourceExecutionService;
import com.wtl.novel.booksource.service.BookSourceRuleValidator;
import com.wtl.novel.booksource.model.SourceBook;
import com.wtl.novel.booksource.model.SourceContent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/book-sources")
public class BookSourceController {
    private final BookSourceImportService importService;
    private final BookSourceExecutionService executionService;

    public BookSourceController(BookSourceImportService importService, BookSourceExecutionService executionService) {
        this.importService = importService;
        this.executionService = executionService;
    }

    @PostMapping("/import")
    public BookSourceImportService.ImportResult importSources(@RequestBody SourceJsonRequest request) {
        return importService.importSources(request.sourceJson());
    }

    @GetMapping
    public Map<String, List<BookSourceImportService.SourceSummary>> listSources() {
        return Map.of("sources", importService.listSources());
    }

    @PostMapping("/validate")
    public Map<String, Object> validate(@RequestBody SourceJsonRequest request) {
        List<BookSourceRuleValidator.RuleIssue> issues = importService.validateSourceJson(request.sourceJson());
        return Map.of("ok", true, "issues", issues);
    }

    @PostMapping("/search")
    public BookSourceExecutionService.SourceSearchResult search(
            @RequestBody BookSourceExecutionService.SearchRequest request) {
        return executionService.search(request);
    }

    @PostMapping("/detail")
    public SourceBook detail(@RequestBody BookSourceExecutionService.DetailRequest request) {
        return executionService.detail(request);
    }

    @PostMapping("/toc")
    public BookSourceExecutionService.SourceTocResult toc(
            @RequestBody BookSourceExecutionService.TocRequest request) {
        return executionService.toc(request);
    }

    @PostMapping("/content")
    public SourceContent content(
            @RequestBody BookSourceExecutionService.ContentRequest request) {
        return executionService.content(request);
    }

    public record SourceJsonRequest(String sourceJson) {
    }
}
