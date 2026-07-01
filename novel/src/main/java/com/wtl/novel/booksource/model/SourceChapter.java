package com.wtl.novel.booksource.model;

public record SourceChapter(
        String chapterKey,
        int index,
        String title,
        String chapterUrl,
        boolean isVip,
        boolean isPay,
        boolean isVolume
) {
}
