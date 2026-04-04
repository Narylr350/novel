package com.wtl.novel.Service;

import com.wtl.novel.entity.Chapter;
import com.wtl.novel.util.ObfuscateFontOTF;
import com.wtl.novel.repository.ChapterRepository;
import com.wtl.novel.repository.ChapterSyncRepository;
import com.wtl.novel.repository.UserChapterEditRepository;
import com.wtl.novel.repository.UserGlossaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChapterServiceReaderModeTest {

    @Test
    void readerModeTreatsMissingUserChapterEditTableAsNoVersions() {
        ChapterService service = new ChapterService();
        UserChapterEditRepository repository = mock(UserChapterEditRepository.class);
        ReflectionTestUtils.setField(service, "userChapterEditRepository", repository);
        ReflectionTestUtils.setField(service, "appUiMode", "reader");

        when(repository.findUserIdAndUsernameByNovelIdAndChapterId(6006108L))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel_lite_validation.user_chapter_edit' doesn't exist"));

        var result = service.findAllContentVersion(6006108L);

        assertEquals(List.of(), result);
    }

    @Test
    void maintainerModeStillSurfacesMissingUserChapterEditTable() {
        ChapterService service = new ChapterService();
        UserChapterEditRepository repository = mock(UserChapterEditRepository.class);
        ReflectionTestUtils.setField(service, "userChapterEditRepository", repository);
        ReflectionTestUtils.setField(service, "appUiMode", "maintainer");

        when(repository.findUserIdAndUsernameByNovelIdAndChapterId(6006108L))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel.user_chapter_edit' doesn't exist"));

        assertThrows(InvalidDataAccessResourceUsageException.class,
                () -> service.findAllContentVersion(6006108L));
    }

    @Test
    void readerModeTreatsMissingChapterSyncTableAsPrimaryChapterRead() {
        ChapterService service = new ChapterService();
        ChapterRepository chapterRepository = mock(ChapterRepository.class);
        ChapterSyncRepository chapterSyncRepository = mock(ChapterSyncRepository.class);

        ReflectionTestUtils.setField(service, "chapterRepository", chapterRepository);
        ReflectionTestUtils.setField(service, "chapterSyncRepository", chapterSyncRepository);
        ReflectionTestUtils.setField(service, "chapterScalingUpOneRepository", null);
        ReflectionTestUtils.setField(service, "appUiMode", "reader");

        Chapter chapter = new Chapter();
        chapter.setId(6006108L);
        chapter.setNovelId(353498L);
        chapter.setTitle("测试章节");
        chapter.setChapterNumber(1);
        chapter.setUpdatedAt(new Date());
        chapter.setContent("第一段\n第二段");

        when(chapterSyncRepository.findByChapterId(6006108L))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel_lite_validation.chapter_sync' doesn't exist"));
        when(chapterRepository.findByIdAndIsDeletedFalse(6006108L)).thenReturn(chapter);

        var result = service.findByIdAndIsDeletedFalse(6006108L);

        assertNotNull(result);
        assertEquals(6006108L, result.getId());
        assertEquals("测试章节", result.getTitle());
    }

    @Test
    void readerModeTreatsMissingUserGlossaryTableAsNoGlossaryTerms() {
        ChapterService service = new ChapterService();
        ChapterRepository chapterRepository = mock(ChapterRepository.class);
        ChapterSyncRepository chapterSyncRepository = mock(ChapterSyncRepository.class);
        UserGlossaryRepository userGlossaryRepository = mock(UserGlossaryRepository.class);
        ChapterCommentService chapterCommentService = mock(ChapterCommentService.class);
        ObfuscateFontOTF obfuscateFontOTF = new ObfuscateFontOTF();
        obfuscateFontOTF.fontMap = new HashMap<>();

        ReflectionTestUtils.setField(service, "chapterRepository", chapterRepository);
        ReflectionTestUtils.setField(service, "chapterSyncRepository", chapterSyncRepository);
        ReflectionTestUtils.setField(service, "chapterScalingUpOneRepository", null);
        ReflectionTestUtils.setField(service, "userGlossaryRepository", userGlossaryRepository);
        ReflectionTestUtils.setField(service, "chapterCommentService", chapterCommentService);
        ReflectionTestUtils.setField(service, "obfuscateFontOTF", obfuscateFontOTF);
        ReflectionTestUtils.setField(service, "appUiMode", "reader");

        Chapter chapter = new Chapter();
        chapter.setId(6006108L);
        chapter.setNovelId(353498L);
        chapter.setTitle("测试章节");
        chapter.setChapterNumber(1);
        chapter.setUpdatedAt(new Date());
        chapter.setOwnPhoto(false);
        chapter.setContent("第一段\n第二段");

        when(chapterSyncRepository.findByChapterId(6006108L)).thenReturn(Optional.empty());
        when(chapterRepository.findByIdAndIsDeletedFalse(6006108L)).thenReturn(chapter);
        when(chapterRepository.findIdByNovelIdAndChapterNumberAndIsDeletedFalse(353498L, 2)).thenReturn(null);
        when(userGlossaryRepository.findByNovelId(353498L))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel_lite_validation.user_glossary' doesn't exist"));
        when(chapterCommentService.countByChapterIdGroupByTextNum(6006108L)).thenReturn(List.of());

        var result = service.findChapterById(6006108L, -1L);

        assertNotNull(result);
        assertEquals(6006108L, result.getId());
        assertEquals("第一段\n第二段", result.getContent());
    }
}
