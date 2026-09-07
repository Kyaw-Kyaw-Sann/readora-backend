package com.readora.backend.repository;

import com.readora.backend.entity.Book;
import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

        boolean existsByIsbn(String isbn);

        boolean existsByIsbnAndIdNot(String isbn, Long id);

        @Query(value = """
                        SELECT DISTINCT b
                        FROM Book b
                        LEFT JOIN b.categories c
                        WHERE (:status IS NULL OR b.status = :status)
                        AND (:accessType IS NULL OR b.accessType = :accessType)
                        AND (:categoryId IS NULL OR c.id = :categoryId)
                        """, countQuery = """
                        SELECT COUNT(DISTINCT b)
                        FROM Book b
                        LEFT JOIN b.categories c
                        WHERE (:status IS NULL OR b.status = :status)
                        AND (:accessType IS NULL OR b.accessType = :accessType)
                        AND (:categoryId IS NULL OR c.id = :categoryId)
                        """)
        Page<Book> findAdminBooks(@Param("status") BookStatus status, @Param("accessType") BookAccessType accessType,
                        @Param("categoryId") Long categoryId, Pageable pageable);

        @Query(value = """
                        SELECT DISTINCT b
                        FROM Book b
                        LEFT JOIN b.categories c
                        WHERE (
                            LOWER(b.title) LIKE :searchPattern
                            OR LOWER(b.author) LIKE :searchPattern
                        )
                        AND (:status IS NULL OR b.status = :status)
                        AND (:accessType IS NULL OR b.accessType = :accessType)
                        AND (:categoryId IS NULL OR c.id = :categoryId)
                        """, countQuery = """
                        SELECT COUNT(DISTINCT b)
                        FROM Book b
                        LEFT JOIN b.categories c
                        WHERE (
                            LOWER(b.title) LIKE :searchPattern
                            OR LOWER(b.author) LIKE :searchPattern
                        )
                        AND (:status IS NULL OR b.status = :status)
                        AND (:accessType IS NULL OR b.accessType = :accessType)
                        AND (:categoryId IS NULL OR c.id = :categoryId)
                        """)
        Page<Book> findAdminBooksBySearch(@Param("searchPattern") String searchPattern,
                        @Param("status") BookStatus status, @Param("accessType") BookAccessType accessType,
                        @Param("categoryId") Long categoryId, Pageable pageable);

        @Query(value = """
                        SELECT DISTINCT b
                        FROM Book b
                        LEFT JOIN b.categories c
                        WHERE b.status = com.readora.backend.enums.BookStatus.PUBLISHED
                        AND (:accessType IS NULL OR b.accessType = :accessType)
                        AND (:categoryId IS NULL OR c.id = :categoryId)
                        """, countQuery = """
                        SELECT COUNT(DISTINCT b)
                        FROM Book b
                        LEFT JOIN b.categories c
                        WHERE b.status = com.readora.backend.enums.BookStatus.PUBLISHED
                        AND (:accessType IS NULL OR b.accessType = :accessType)
                        AND (:categoryId IS NULL OR c.id = :categoryId)
                        """)
        Page<Book> findPublishedBooks(@Param("accessType") BookAccessType accessType,
                        @Param("categoryId") Long categoryId, Pageable pageable);

        @Query(value = """
                        SELECT DISTINCT b
                        FROM Book b
                        LEFT JOIN b.categories c
                        WHERE b.status = com.readora.backend.enums.BookStatus.PUBLISHED
                        AND (
                            LOWER(b.title) LIKE :searchPattern
                            OR LOWER(b.author) LIKE :searchPattern
                        )
                        AND (:accessType IS NULL OR b.accessType = :accessType)
                        AND (:categoryId IS NULL OR c.id = :categoryId)
                        """, countQuery = """
                        SELECT COUNT(DISTINCT b)
                        FROM Book b
                        LEFT JOIN b.categories c
                        WHERE b.status = com.readora.backend.enums.BookStatus.PUBLISHED
                        AND (
                            LOWER(b.title) LIKE :searchPattern
                            OR LOWER(b.author) LIKE :searchPattern
                        )
                        AND (:accessType IS NULL OR b.accessType = :accessType)
                        AND (:categoryId IS NULL OR c.id = :categoryId)
                        """)
        Page<Book> findPublishedBooksBySearch(@Param("searchPattern") String searchPattern,
                        @Param("accessType") BookAccessType accessType, @Param("categoryId") Long categoryId,
                        Pageable pageable);

        Optional<Book> findByIdAndStatus(Long id, BookStatus status);

        List<Book> findAllByStatus(BookStatus status);

        List<Book> findTop10ByStatusOrderByViewCountDescCreatedAtDesc(BookStatus status);

        long countByStatus(BookStatus status);

        long countByAccessType(BookAccessType accessType);
}