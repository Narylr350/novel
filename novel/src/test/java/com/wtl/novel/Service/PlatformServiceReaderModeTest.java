package com.wtl.novel.Service;

import com.wtl.novel.entity.Platform;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.repository.PlatformRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PlatformServiceReaderModeTest {

    @Test
    void readerModeUsesNovelPlatformsForNovelType() {
        PlatformService service = new PlatformService();
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        NovelRepository novelRepository = mock(NovelRepository.class);
        ReflectionTestUtils.setField(service, "platformRepository", platformRepository);
        ReflectionTestUtils.setField(service, "novelRepository", novelRepository);
        ReflectionTestUtils.setField(service, "appUiMode", "reader");

        when(novelRepository.findDistinctPlatforms()).thenReturn(List.of("shutu", "upload"));

        List<Platform> result = service.getPlatformsByType("novel");

        assertEquals(2, result.size());
        assertEquals("shutu", result.get(0).getPlatformName());
        assertEquals("novel", result.get(0).getPlatformType());
        assertEquals("upload", result.get(1).getPlatformName());
        verify(novelRepository).findDistinctPlatforms();
        verifyNoInteractions(platformRepository);
    }

    @Test
    void maintainerModeKeepsPlatformRepositoryLookup() {
        PlatformService service = new PlatformService();
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        NovelRepository novelRepository = mock(NovelRepository.class);
        ReflectionTestUtils.setField(service, "platformRepository", platformRepository);
        ReflectionTestUtils.setField(service, "novelRepository", novelRepository);
        ReflectionTestUtils.setField(service, "appUiMode", "maintainer");

        Platform platform = new Platform();
        platform.setPlatformName("novelPia");
        when(platformRepository.findPlatformsByPlatformType("novel")).thenReturn(List.of(platform));

        List<Platform> result = service.getPlatformsByType("novel");

        assertEquals(1, result.size());
        assertEquals("novelPia", result.getFirst().getPlatformName());
        verify(platformRepository).findPlatformsByPlatformType("novel");
        verifyNoInteractions(novelRepository);
    }
}
