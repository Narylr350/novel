package com.wtl.novel.booksource.service;

import com.wtl.novel.booksource.entity.BookSourceEntity;
import com.wtl.novel.booksource.repository.BookSourceRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookSourceImportServiceTest {

    @Test
    void importSourcesParsesArrayAndSavesSourceMetadataWithRawJson() {
        BookSourceRepository repository = mock(BookSourceRepository.class);
        BookSourceRuleValidator validator = new BookSourceRuleValidator();
        BookSourceImportService service = new BookSourceImportService(repository, validator);
        when(repository.save(any(BookSourceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookSourceImportService.ImportResult result = service.importSources("""
                [{
                  "bookSourceUrl": "https://example.com",
                  "bookSourceName": "Example",
                  "bookSourceGroup": "Demo",
                  "searchUrl": "https://example.com/search?q={{key}}"
                }]
                """);

        assertEquals(1, result.count());
        assertEquals(1, result.sources().size());
        assertEquals("https://example.com", result.sources().getFirst().bookSourceUrl());
        assertEquals("Example", result.sources().getFirst().bookSourceName());
        assertFalse(result.sources().getFirst().sourceId().isBlank());
        verify(repository).save(any(BookSourceEntity.class));
    }

    @Test
    void listSourcesMapsEntitiesToSummaries() {
        BookSourceRepository repository = mock(BookSourceRepository.class);
        BookSourceImportService service = new BookSourceImportService(repository, new BookSourceRuleValidator());
        BookSourceEntity entity = new BookSourceEntity();
        entity.setSourceId("source-id");
        entity.setBookSourceUrl("https://example.com");
        entity.setBookSourceName("Example");
        entity.setEnabled(true);
        when(repository.findAll()).thenReturn(List.of(entity));

        List<BookSourceImportService.SourceSummary> result = service.listSources();

        assertEquals(1, result.size());
        assertEquals("source-id", result.getFirst().sourceId());
        assertEquals("https://example.com", result.getFirst().bookSourceUrl());
        assertEquals("Example", result.getFirst().bookSourceName());
    }
}
