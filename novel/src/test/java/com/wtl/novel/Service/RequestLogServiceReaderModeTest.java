package com.wtl.novel.Service;

import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Dictionary;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.RequestLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestLogServiceReaderModeTest {

    @Test
    void readerModeAllowsProtectedReadWhenRequestLogTableIsMissing() {
        RequestLogService service = new RequestLogService();
        RequestLogRepository requestLogRepository = mock(RequestLogRepository.class);
        DictionaryRepository dictionaryRepository = mock(DictionaryRepository.class);
        Credential credential = new Credential();
        credential.setId(7L);
        Dictionary limitRequest = new Dictionary();
        limitRequest.setValueField("20");

        ReflectionTestUtils.setField(service, "requestLogRepository", requestLogRepository);
        ReflectionTestUtils.setField(service, "dictionaryRepository", dictionaryRepository);
        ReflectionTestUtils.setField(service, "limitUrl", List.of());
        ReflectionTestUtils.setField(service, "appUiMode", "reader");

        when(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("limitRequest")).thenReturn(limitRequest);
        when(requestLogRepository.findByCredentialIdAndRequestTimeAfter(any(), any()))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel_lite_validation.request_log' doesn't exist"));

        boolean allowed = service.checkRequestLimit(credential, "/api/chapters/getChapterByIdApi/6006108");

        assertTrue(allowed);
        verify(requestLogRepository, never()).save(any());
    }

    @Test
    void maintainerModeStillSurfacesMissingRequestLogTable() {
        RequestLogService service = new RequestLogService();
        RequestLogRepository requestLogRepository = mock(RequestLogRepository.class);
        DictionaryRepository dictionaryRepository = mock(DictionaryRepository.class);
        Credential credential = new Credential();
        credential.setId(7L);
        Dictionary limitRequest = new Dictionary();
        limitRequest.setValueField("20");

        ReflectionTestUtils.setField(service, "requestLogRepository", requestLogRepository);
        ReflectionTestUtils.setField(service, "dictionaryRepository", dictionaryRepository);
        ReflectionTestUtils.setField(service, "limitUrl", List.of());
        ReflectionTestUtils.setField(service, "appUiMode", "maintainer");

        when(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("limitRequest")).thenReturn(limitRequest);
        when(requestLogRepository.findByCredentialIdAndRequestTimeAfter(any(), any()))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel.request_log' doesn't exist"));

        assertThrows(InvalidDataAccessResourceUsageException.class,
                () -> service.checkRequestLimit(credential, "/api/chapters/getChapterByIdApi/6006108"));
    }
}
