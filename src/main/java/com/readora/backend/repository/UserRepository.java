package com.readora.backend.repository;

import com.readora.backend.entity.User;
import com.readora.backend.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByEmailVerifiedTrue();

    long countByEmailVerifiedFalse();

    @Query(value = """
            SELECT u
            FROM User u
            WHERE (
                :verified IS NULL
                OR u.emailVerified = :verified
            )
            AND (
                :premium IS NULL

                OR (
                    :premium = true
                    AND EXISTS (
                        SELECT s.id
                        FROM Subscription s
                        WHERE s.user.id = u.id
                        AND s.status = :activeStatus
                        AND s.expiresAt > :now
                    )
                )

                OR (
                    :premium = false
                    AND NOT EXISTS (
                        SELECT s.id
                        FROM Subscription s
                        WHERE s.user.id = u.id
                        AND s.status = :activeStatus
                        AND s.expiresAt > :now
                    )
                )
            )
            """, countQuery = """
            SELECT COUNT(u)
            FROM User u
            WHERE (
                :verified IS NULL
                OR u.emailVerified = :verified
            )
            AND (
                :premium IS NULL

                OR (
                    :premium = true
                    AND EXISTS (
                        SELECT s.id
                        FROM Subscription s
                        WHERE s.user.id = u.id
                        AND s.status = :activeStatus
                        AND s.expiresAt > :now
                    )
                )

                OR (
                    :premium = false
                    AND NOT EXISTS (
                        SELECT s.id
                        FROM Subscription s
                        WHERE s.user.id = u.id
                        AND s.status = :activeStatus
                        AND s.expiresAt > :now
                    )
                )
            )
            """)
    Page<User> findAdminUsers(@Param("verified") Boolean verified, @Param("premium") Boolean premium,
            @Param("activeStatus") SubscriptionStatus activeStatus, @Param("now") LocalDateTime now, Pageable pageable);

    @Query(value = """
            SELECT u
            FROM User u
            WHERE (
                LOWER(u.name) LIKE :searchPattern
                OR LOWER(u.email) LIKE :searchPattern
            )
            AND (
                :verified IS NULL
                OR u.emailVerified = :verified
            )
            AND (
                :premium IS NULL

                OR (
                    :premium = true
                    AND EXISTS (
                        SELECT s.id
                        FROM Subscription s
                        WHERE s.user.id = u.id
                        AND s.status = :activeStatus
                        AND s.expiresAt > :now
                    )
                )

                OR (
                    :premium = false
                    AND NOT EXISTS (
                        SELECT s.id
                        FROM Subscription s
                        WHERE s.user.id = u.id
                        AND s.status = :activeStatus
                        AND s.expiresAt > :now
                    )
                )
            )
            """, countQuery = """
            SELECT COUNT(u)
            FROM User u
            WHERE (
                LOWER(u.name) LIKE :searchPattern
                OR LOWER(u.email) LIKE :searchPattern
            )
            AND (
                :verified IS NULL
                OR u.emailVerified = :verified
            )
            AND (
                :premium IS NULL

                OR (
                    :premium = true
                    AND EXISTS (
                        SELECT s.id
                        FROM Subscription s
                        WHERE s.user.id = u.id
                        AND s.status = :activeStatus
                        AND s.expiresAt > :now
                    )
                )

                OR (
                    :premium = false
                    AND NOT EXISTS (
                        SELECT s.id
                        FROM Subscription s
                        WHERE s.user.id = u.id
                        AND s.status = :activeStatus
                        AND s.expiresAt > :now
                    )
                )
            )
            """)
    Page<User> findAdminUsersBySearch(@Param("searchPattern") String searchPattern, @Param("verified") Boolean verified,
            @Param("premium") Boolean premium, @Param("activeStatus") SubscriptionStatus activeStatus,
            @Param("now") LocalDateTime now, Pageable pageable);
}