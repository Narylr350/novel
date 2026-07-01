package com.wtl.novel.booksource.service;

import com.wtl.novel.booksource.model.BookSource;
import com.wtl.novel.booksource.model.TocRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookSourceRuleValidator {

    public record RuleIssue(String field, String rule, String severity, String message) {
    }

    public List<RuleIssue> validate(BookSource source) {
        List<RuleIssue> issues = new ArrayList<>();
        if (isBlank(source.getBookSourceUrl())) {
            issues.add(new RuleIssue("bookSourceUrl", "", "error", "bookSourceUrl is required"));
        }
        if (isBlank(source.getBookSourceName())) {
            issues.add(new RuleIssue("bookSourceName", "", "error", "bookSourceName is required"));
        }
        String searchUrl = source.getSearchUrl();
        if (!isBlank(searchUrl) && containsWebView(searchUrl)) {
            issues.add(new RuleIssue(
                    "searchUrl",
                    searchUrl,
                    "error",
                    "searchUrl should not contain webView; render mode belongs to chapter content"
            ));
        }
        TocRule tocRule = source.getRuleToc();
        String chapterUrl = tocRule != null ? tocRule.getChapterUrl() : null;
        if (!isBlank(chapterUrl) && containsWebView(chapterUrl) && chapterUrl.contains("@")
                && !chapterUrl.contains("##$##")) {
            issues.add(new RuleIssue(
                    "ruleToc.chapterUrl",
                    chapterUrl,
                    "error",
                    "CSS chapterUrl with webView must use ##$## before URL options"
            ));
        }
        return issues;
    }

    private boolean containsWebView(String value) {
        return value.toLowerCase().contains("webview");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
