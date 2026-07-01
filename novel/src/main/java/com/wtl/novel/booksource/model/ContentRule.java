package com.wtl.novel.booksource.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentRule {
    private String content;
    private String title;
    private String nextContentUrl;
    private String webJs;
    private String sourceRegex;
    private String replaceRegex;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNextContentUrl() {
        return nextContentUrl;
    }

    public void setNextContentUrl(String nextContentUrl) {
        this.nextContentUrl = nextContentUrl;
    }

    public String getWebJs() {
        return webJs;
    }

    public void setWebJs(String webJs) {
        this.webJs = webJs;
    }

    public String getSourceRegex() {
        return sourceRegex;
    }

    public void setSourceRegex(String sourceRegex) {
        this.sourceRegex = sourceRegex;
    }

    public String getReplaceRegex() {
        return replaceRegex;
    }

    public void setReplaceRegex(String replaceRegex) {
        this.replaceRegex = replaceRegex;
    }
}
