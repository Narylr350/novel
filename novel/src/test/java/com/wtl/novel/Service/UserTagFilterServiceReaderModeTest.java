package com.wtl.novel.Service;

import com.wtl.novel.repository.TagRepository;
import com.wtl.novel.repository.UserTagFilterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTagFilterServiceReaderModeTest {

    @Test
    void readerModeTreatsMissingUserTagFilterTableAsEmptySelection() {
        UserTagFilterRepository repository = mock(UserTagFilterRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        UserTagFilterService service = new UserTagFilterService(repository, tagRepository);
        ReflectionTestUtils.setField(service, "appUiMode", "reader");

        when(repository.findAllByUserId(1L))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel_lite_validation.user_tag_filter' doesn't exist"));

        List<?> result = assertDoesNotThrow(() -> service.getFilterTag(1L));

        assertEquals(0, result.size());
    }

    @Test
    void maintainerModeStillSurfacesMissingUserTagFilterTable() {
        UserTagFilterRepository repository = mock(UserTagFilterRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        UserTagFilterService service = new UserTagFilterService(repository, tagRepository);
        ReflectionTestUtils.setField(service, "appUiMode", "maintainer");

        when(repository.findAllByUserId(1L))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel.user_tag_filter' doesn't exist"));

        assertThrows(InvalidDataAccessResourceUsageException.class, () -> service.getFilterTag(1L));
    }
}
