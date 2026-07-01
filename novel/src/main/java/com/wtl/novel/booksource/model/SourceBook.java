package com.wtl.novel.booksource.model;

public record SourceBook(
        String sourceId,
        String bookKey,
        String bookUrl,
        String name,
        String author,
        String kind,
        String coverUrl,
        String intro,
        String lastChapter,
        String wordCount,
        String tocUrl,
        String updateTime
) {
}
