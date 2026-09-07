package com.readora.backend.repository;

import com.readora.backend.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUser_IdAndBook_Id(Long userId, Long bookId);

    Optional<Favorite> findByUser_IdAndBook_Id(Long userId, Long bookId);

    List<Favorite> findAllByUser_Id(Long userId);

    Page<Favorite> findAllByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}