package com.readora.backend.repository;

import com.readora.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUser_IdAndBook_Id(Long userId, Long bookId);

    Optional<Review> findByUser_IdAndBook_Id(Long userId, Long bookId);

    Page<Review> findAllByBook_Id(Long bookId, Pageable pageable);

    long countByBook_Id(Long bookId);

    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.book.id = :bookId
            """)
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);

    @Query(value = """
            SELECT r
            FROM Review r
            WHERE (
                :rating IS NULL
                OR r.rating = :rating
            )
            """, countQuery = """
            SELECT COUNT(r)
            FROM Review r
            WHERE (
                :rating IS NULL
                OR r.rating = :rating
            )
            """)
    Page<Review> findAdminReviews(@Param("rating") Integer rating, Pageable pageable);

    @Query(value = """
            SELECT r
            FROM Review r
            WHERE (
                LOWER(r.user.name) LIKE :searchPattern
                OR LOWER(r.user.email) LIKE :searchPattern
                OR LOWER(r.book.title) LIKE :searchPattern
                OR LOWER(r.comment) LIKE :searchPattern
            )
            AND (
                :rating IS NULL
                OR r.rating = :rating
            )
            """, countQuery = """
            SELECT COUNT(r)
            FROM Review r
            WHERE (
                LOWER(r.user.name) LIKE :searchPattern
                OR LOWER(r.user.email) LIKE :searchPattern
                OR LOWER(r.book.title) LIKE :searchPattern
                OR LOWER(r.comment) LIKE :searchPattern
            )
            AND (
                :rating IS NULL
                OR r.rating = :rating
            )
            """)
    Page<Review> findAdminReviewsBySearch(@Param("searchPattern") String searchPattern, @Param("rating") Integer rating,
            Pageable pageable);

    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            """)
    Double findGlobalAverageRating();
}