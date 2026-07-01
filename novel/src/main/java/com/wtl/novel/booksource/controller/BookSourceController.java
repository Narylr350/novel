package com.wtl.novel.booksource.controller;

import com.wtl.novel.booksource.service.BookSourceImportService;
import com.wtl.novel.booksource.service.BookSourceRuleValidator;
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

    public BookSourceController(BookSourceImportService importService) {
        this.importService = importService;
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

    public record SourceJsonRequest(String sourceJson) {
    }
}
