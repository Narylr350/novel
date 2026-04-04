package com.wtl.novel.Service;

import com.wtl.novel.repository.UserAccessLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAccessLogServiceReaderModeTest {

    @Test
    void readerModeSkipsUserAccessLoggingWhenTableIsMissing() {
        UserAccessLogRepository repository = mock(UserAccessLogRepository.class);
        UserAccessLogService service = new UserAccessLogService(repository);
        ReflectionTestUtils.setField(service, "appUiMode", "reader");

        when(repository.findByUserIdAndVisitDate(any(), any()))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel_lite_validation.user_access_logs' doesn't exist"));

        assertDoesNotThrow(() -> service.findByUserIdAndVisitDate(1L, "127.0.0.1", "Mozilla/5.0"));
        verify(repository, never()).save(any());
    }

    @Test
    void maintainerModeStillSurfacesMissingUserAccessLogTable() {
        UserAccessLogRepository repository = mock(UserAccessLogRepository.class);
        UserAccessLogService service = new UserAccessLogService(repository);
        ReflectionTestUtils.setField(service, "appUiMode", "maintainer");

        when(repository.findByUserIdAndVisitDate(any(), any()))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel.user_access_logs' doesn't exist"));

        assertThrows(InvalidDataAccessResourceUsageException.class,
                () -> service.findByUserIdAndVisitDate(1L, "127.0.0.1", "Mozilla/5.0"));
    }
}
