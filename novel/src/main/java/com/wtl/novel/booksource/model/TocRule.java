package com.wtl.novel.booksource.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TocRule {
    private String chapterList;
    private String chapterName;
    private String chapterUrl;
    private String isVolume;
    private String isVip;
    private String isPay;
    private String nextTocUrl;

    public String getChapterList() {
        return chapterList;
    }

    public void setChapterList(String chapterList) {
        this.chapterList = chapterList;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }

    public String getIsVolume() {
        return isVolume;
    }

    public void setIsVolume(String isVolume) {
        this.isVolume = isVolume;
    }

    public String getIsVip() {
        return isVip;
    }

    public void setIsVip(String isVip) {
        this.isVip = isVip;
    }

    public String getIsPay() {
        return isPay;
    }

    public void setIsPay(String isPay) {
        this.isPay = isPay;
    }

    public String getNextTocUrl() {
        return nextTocUrl;
    }

    public void setNextTocUrl(String nextTocUrl) {
        this.nextTocUrl = nextTocUrl;
    }
}
