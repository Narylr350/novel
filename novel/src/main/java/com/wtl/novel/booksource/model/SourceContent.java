package com.wtl.novel.booksource.model;

public record SourceContent(
        String sourceId,
        String bookKey,
        String chapterKey,
        String title,
        String content,
        String rendererMode,
        String rendererProfile,
        String finalUrl
) {
}
