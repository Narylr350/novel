package com.wtl.novel.Service;

import com.wtl.novel.entity.Tag;
import com.wtl.novel.entity.User;
import com.wtl.novel.entity.UserTagFilter;
import com.wtl.novel.repository.TagRepository;
import com.wtl.novel.repository.UserTagFilterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserTagFilterService {
    private static final Logger log = LoggerFactory.getLogger(UserTagFilterService.class);

    @Value("${app.ui.mode:reader}")
    private String appUiMode;

    public void filterTag(Long id,Long userId) {
        Optional<UserTagFilter> tagFilter = repository.findByIdAndUserId(id,userId);
        if (tagFilter.isPresent()) {
            repository.delete(tagFilter.get());
        } else {
            Tag tag = tagRepository.getReferenceById(id);
            UserTagFilter userTagFilter = new UserTagFilter();
            userTagFilter.setUserId(userId);
            userTagFilter.setTagId(id);
            userTagFilter.setTagName(tag.getName());
            repository.save(userTagFilter);
        }
    }
    public List<UserTagFilter> getFilterTag(Long userId) {
        try {
            return repository.findAllByUserId(userId);
        } catch (InvalidDataAccessResourceUsageException ex) {
            if (isReaderModeMissingTable(ex)) {
                // Lite reader packages intentionally omit this maintainer-side table.
                log.warn("reader mode missing user_tag_filter table, fallback to empty selection. userId={}", userId);
                return Collections.emptyList();
            }
            throw ex;
        }
    }

    private final UserTagFilterRepository repository;
    private final TagRepository tagRepository;

    public UserTagFilterService(UserTagFilterRepository repository, TagRepository tagRepository) {
        this.repository = repository;
        this.tagRepository = tagRepository;
    }

    private boolean isReaderModeMissingTable(InvalidDataAccessResourceUsageException ex) {
        return "reader".equalsIgnoreCase(appUiMode)
                && ex.getMessage() != null
                && ex.getMessage().contains("user_tag_filter");
    }

}
