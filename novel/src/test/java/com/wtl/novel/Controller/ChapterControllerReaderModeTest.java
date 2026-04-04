package com.wtl.novel.Controller;

import com.wtl.novel.CDO.ChapterCDO;
import com.wtl.novel.Service.ChapterCommentService;
import com.wtl.novel.Service.ChapterService;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.entity.Chapter;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Dictionary;
import com.wtl.novel.entity.User;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.RequestLogRepository;
import com.wtl.novel.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChapterControllerReaderModeTest {

    @Test
    void readerModeSkipsRequestRewardCountWhenRequestLogTableIsMissing() {
        ChapterController controller = new ChapterController();
        RequestLogRepository requestLogRepository = mock(RequestLogRepository.class);
        DictionaryRepository dictionaryRepository = mock(DictionaryRepository.class);

        ReflectionTestUtils.setField(controller, "requestLogRepository", requestLogRepository);
        ReflectionTestUtils.setField(controller, "dictionaryRepository", dictionaryRepository);
        ReflectionTestUtils.setField(controller, "userRepository", mock(UserRepository.class));
        ReflectionTestUtils.setField(controller, "appUiMode", "reader");

        Credential credential = new Credential();

        when(requestLogRepository.findByCredentialIdAndRequestTimeAfter(any(), any()))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel_lite_validation.request_log' doesn't exist"));

        assertDoesNotThrow(() -> controller.requestCount(credential));
    }

    @Test
    void maintainerModeStillSurfacesMissingRequestLogTableInRewardCount() {
        ChapterController controller = new ChapterController();
        RequestLogRepository requestLogRepository = mock(RequestLogRepository.class);
        DictionaryRepository dictionaryRepository = mock(DictionaryRepository.class);

        ReflectionTestUtils.setField(controller, "requestLogRepository", requestLogRepository);
        ReflectionTestUtils.setField(controller, "dictionaryRepository", dictionaryRepository);
        ReflectionTestUtils.setField(controller, "userRepository", mock(UserRepository.class));
        ReflectionTestUtils.setField(controller, "appUiMode", "maintainer");

        Credential credential = new Credential();

        when(requestLogRepository.findByCredentialIdAndRequestTimeAfter(any(), any()))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel.request_log' doesn't exist"));

        assertThrows(InvalidDataAccessResourceUsageException.class, () -> controller.requestCount(credential));
    }

    @Test
    void chapterApiLeavesEmptyContentUnencrypted() throws Exception {
        ChapterController controller = new ChapterController();
        ChapterService chapterService = mock(ChapterService.class);
        CredentialService credentialService = mock(CredentialService.class);
        RequestLogRepository requestLogRepository = mock(RequestLogRepository.class);
        DictionaryRepository dictionaryRepository = mock(DictionaryRepository.class);
        ChapterCommentService chapterCommentService = mock(ChapterCommentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ReflectionTestUtils.setField(controller, "chapterService", chapterService);
        ReflectionTestUtils.setField(controller, "credentialService", credentialService);
        ReflectionTestUtils.setField(controller, "requestLogRepository", requestLogRepository);
        ReflectionTestUtils.setField(controller, "dictionaryRepository", dictionaryRepository);
        ReflectionTestUtils.setField(controller, "chapterCommentService", chapterCommentService);
        ReflectionTestUtils.setField(controller, "userRepository", mock(UserRepository.class));
        ReflectionTestUtils.setField(controller, "appUiMode", "reader");

        Credential credential = new Credential();
        credential.setId(1L);
        credential.setToken("reader-token");
        credential.setExpiredAt(java.time.LocalDateTime.now().plusHours(1));
        User user = new User();
        user.setId(1L);
        credential.setUser(user);

        Chapter chapterEntity = new Chapter();
        chapterEntity.setId(6006108L);
        chapterEntity.setNovelId(353498L);
        chapterEntity.setTitle("测试章节");
        chapterEntity.setChapterNumber(1);
        chapterEntity.setContent("");
        ChapterCDO chapter = new ChapterCDO(chapterEntity);

        Dictionary fontVersion = new Dictionary();
        fontVersion.setValueField("10");

        when(request.getHeader("Authorization")).thenReturn("reader-token;sig;ts;nonce");
        when(credentialService.findByToken("reader-token")).thenReturn(credential);
        when(requestLogRepository.findByCredentialIdAndRequestTimeAfter(any(), any())).thenReturn(null);
        when(chapterService.findChapterById(6006108L, 1L)).thenReturn(chapter);
        when(dictionaryRepository.getDictionaryByKeyField("fontVersion")).thenReturn(fontVersion);
        when(chapterCommentService.countByChapterIdGroupByTextNum(6006108L)).thenReturn(java.util.List.of());

        ChapterCDO response = controller.getChapterByIdApi(6006108L, request);

        assertEquals("", response.getContent());
    }
}
