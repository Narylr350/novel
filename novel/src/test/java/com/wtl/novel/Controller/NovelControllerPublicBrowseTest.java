package com.wtl.novel.Controller;

import com.wtl.novel.CDO.NovelCTO;
import com.wtl.novel.CDO.NovelSearchRequest;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.NovelService;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.repository.ChapterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NovelControllerPublicBrowseTest {

    @Test
    void getNovelsByPlatformAllowsAnonymousReaderBrowse() throws Exception {
        NovelController controller = new NovelController();
        NovelService novelService = mock(NovelService.class);
        ChapterRepository chapterRepository = mock(ChapterRepository.class);
        ReflectionTestUtils.setField(controller, "novelService", novelService);
        ReflectionTestUtils.setField(controller, "chapterRepository", chapterRepository);
        ReflectionTestUtils.setField(controller, "credentialService", mock(CredentialService.class));

        Novel novel = new Novel();
        novel.setId(1L);
        novel.setTitle("测试书籍");
        novel.setPlatform("novelPia");
        novel.setFontNumber(12345L);
        novel.setUp(10L);
        NovelCTO novelCTO = new NovelCTO(novel, null, null);
        Page<NovelCTO> page = new PageImpl<>(List.of(novelCTO));

        when(novelService.getNovelsWithPagination(eq("novelPia"), eq("0_1000000000"), eq("0"), any(), isNull()))
                .thenReturn(page);
        when(chapterRepository.getChapterCountsByNovelIds(List.of(1L))).thenReturn(Map.of(1L, 12));
        when(novelService.getTagsForNovels(List.of(1L))).thenReturn(Map.of(1L, List.of("校园")));

        NovelSearchRequest requestBody = new NovelSearchRequest();
        requestBody.setPlatform("novelPia");
        requestBody.setFontNumber("0_1000000000");
        requestBody.setTabIds("0");
        requestBody.setPage(0);
        requestBody.setSize(30);
        requestBody.setSort("up");
        requestBody.setDirection("desc");

        Page<NovelCTO> response = controller.getNovelsByPlatform(requestBody, new MockHttpServletRequest());

        assertEquals(1, response.getContent().size());
        assertEquals(12, response.getContent().getFirst().getChapterNum());
        assertEquals(List.of("校园"), response.getContent().getFirst().getTags());
        verify(novelService).getNovelsWithPagination(eq("novelPia"), eq("0_1000000000"), eq("0"), any(), isNull());
    }
}
