package com.wtl.novel.booksource.repository;

import com.wtl.novel.booksource.entity.BookSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookSourceRepository extends JpaRepository<BookSourceEntity, Long> {
    Optional<BookSourceEntity> findBySourceId(String sourceId);

    Optional<BookSourceEntity> findByBookSourceUrl(String bookSourceUrl);
}
