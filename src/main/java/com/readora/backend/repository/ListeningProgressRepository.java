package com.readora.backend.repository;

import com.readora.backend.entity.ListeningProgress;
import com.readora.backend.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListeningProgressRepository extends JpaRepository<ListeningProgress, Long> {

    Optional<ListeningProgress> findByUser_IdAndBook_Id(Long userId, Long bookId);

    Page<ListeningProgress> findAllByUser_IdAndCompletedFalseAndBook_StatusOrderByLastAccessedAtDesc(Long userId,
            BookStatus status, Pageable pageable);

    Page<ListeningProgress> findAllByUser_IdAndBook_StatusOrderByLastAccessedAtDesc(Long userId, BookStatus status,
            Pageable pageable);
}
