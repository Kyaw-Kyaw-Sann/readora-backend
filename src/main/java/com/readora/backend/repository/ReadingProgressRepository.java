package com.readora.backend.repository;

import com.readora.backend.entity.ReadingProgress;
import com.readora.backend.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {

    Optional<ReadingProgress> findByUser_IdAndBook_Id(Long userId, Long bookId);

    List<ReadingProgress> findAllByUser_Id(Long userId);

    Page<ReadingProgress> findAllByUser_IdAndCompletedFalseAndBook_StatusOrderByLastAccessedAtDesc(Long userId,
            BookStatus status, Pageable pageable);

    Page<ReadingProgress> findAllByUser_IdAndBook_StatusOrderByLastAccessedAtDesc(Long userId, BookStatus status,
            Pageable pageable);
}