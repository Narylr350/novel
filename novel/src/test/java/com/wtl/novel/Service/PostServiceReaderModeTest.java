package com.wtl.novel.Service;

import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.repository.PostCommentRepository;
import com.wtl.novel.repository.PostRepository;
import com.wtl.novel.repository.UserBlacklistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostServiceReaderModeTest {

    @Test
    void readerModeTreatsMissingPostTableAsEmptyNovelCommentsPage() {
        PostService service = new PostService();
        PostRepository postRepository = mock(PostRepository.class);
        ReflectionTestUtils.setField(service, "postRepository", postRepository);
        ReflectionTestUtils.setField(service, "postCommentRepository", mock(PostCommentRepository.class));
        ReflectionTestUtils.setField(service, "userBlacklistRepository", mock(UserBlacklistRepository.class));
        ReflectionTestUtils.setField(service, "novelRepository", mock(NovelRepository.class));
        ReflectionTestUtils.setField(service, "appUiMode", "reader");

        when(postRepository.findByPostTypeAndNovelId(353498L, PageRequest.of(0, 20)))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel_lite_validation.post' doesn't exist"));

        var result = service.getAllPostsByNovelId(353498L, PageRequest.of(0, 20));

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void maintainerModeStillSurfacesMissingPostTable() {
        PostService service = new PostService();
        PostRepository postRepository = mock(PostRepository.class);
        ReflectionTestUtils.setField(service, "postRepository", postRepository);
        ReflectionTestUtils.setField(service, "postCommentRepository", mock(PostCommentRepository.class));
        ReflectionTestUtils.setField(service, "userBlacklistRepository", mock(UserBlacklistRepository.class));
        ReflectionTestUtils.setField(service, "novelRepository", mock(NovelRepository.class));
        ReflectionTestUtils.setField(service, "appUiMode", "maintainer");

        when(postRepository.findByPostTypeAndNovelId(353498L, PageRequest.of(0, 20)))
                .thenThrow(new InvalidDataAccessResourceUsageException("Table 'novel.post' doesn't exist"));

        assertThrows(InvalidDataAccessResourceUsageException.class,
                () -> service.getAllPostsByNovelId(353498L, PageRequest.of(0, 20)));
    }
}
