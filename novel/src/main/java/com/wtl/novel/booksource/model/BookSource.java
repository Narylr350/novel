package com.wtl.novel.booksource.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookSource {
    private String bookSourceUrl;
    private String bookSourceName;
    private String bookSourceGroup;
    private boolean enabled = true;
    private String searchUrl;
    private SearchRule ruleSearch;
    private BookInfoRule ruleBookInfo;
    private TocRule ruleToc;
    private ContentRule ruleContent;

    public String getBookSourceUrl() {
        return bookSourceUrl;
    }

    public void setBookSourceUrl(String bookSourceUrl) {
        this.bookSourceUrl = bookSourceUrl;
    }

    public String getBookSourceName() {
        return bookSourceName;
    }

    public void setBookSourceName(String bookSourceName) {
        this.bookSourceName = bookSourceName;
    }

    public String getBookSourceGroup() {
        return bookSourceGroup;
    }

    public void setBookSourceGroup(String bookSourceGroup) {
        this.bookSourceGroup = bookSourceGroup;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public SearchRule getRuleSearch() {
        return ruleSearch;
    }

    public void setRuleSearch(SearchRule ruleSearch) {
        this.ruleSearch = ruleSearch;
    }

    public BookInfoRule getRuleBookInfo() {
        return ruleBookInfo;
    }

    public void setRuleBookInfo(BookInfoRule ruleBookInfo) {
        this.ruleBookInfo = ruleBookInfo;
    }

    public TocRule getRuleToc() {
        return ruleToc;
    }

    public void setRuleToc(TocRule ruleToc) {
        this.ruleToc = ruleToc;
    }

    public ContentRule getRuleContent() {
        return ruleContent;
    }

    public void setRuleContent(ContentRule ruleContent) {
        this.ruleContent = ruleContent;
    }
}
