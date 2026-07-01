package com.wtl.novel.booksource.service;

import com.wtl.novel.booksource.model.BookSource;
import com.wtl.novel.booksource.model.TocRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookSourceRuleValidatorTest {

    @Test
    void validateRejectsWebViewInSearchUrl() {
        BookSource source = new BookSource();
        source.setBookSourceUrl("https://example.com");
        source.setBookSourceName("Example");
        source.setSearchUrl("https://example.com/search?q={{key}},{\"webView\":true}");

        List<BookSourceRuleValidator.RuleIssue> issues = new BookSourceRuleValidator().validate(source);

        assertEquals(1, issues.size());
        assertEquals("searchUrl", issues.getFirst().field());
        assertEquals("error", issues.getFirst().severity());
    }

    @Test
    void validateRejectsCssChapterUrlWebViewWithoutChainOperator() {
        BookSource source = new BookSource();
        source.setBookSourceUrl("https://example.com");
        source.setBookSourceName("Example");
        TocRule tocRule = new TocRule();
        tocRule.setChapterUrl("a@href,{\"webView\":true}");
        source.setRuleToc(tocRule);

        List<BookSourceRuleValidator.RuleIssue> issues = new BookSourceRuleValidator().validate(source);

        assertEquals(1, issues.size());
        assertEquals("ruleToc.chapterUrl", issues.getFirst().field());
        assertEquals("error", issues.getFirst().severity());
    }
}
