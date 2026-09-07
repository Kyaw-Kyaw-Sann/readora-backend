package com.readora.backend.repository;

import com.readora.backend.entity.Subscription;
import com.readora.backend.enums.SubscriptionPlan;
import com.readora.backend.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByUser_IdOrderByStartedAtDesc(Long userId);

    Optional<Subscription> findTopByUser_IdAndStatusOrderByStartedAtDesc(Long userId, SubscriptionStatus status);

    List<Subscription> findAllByUser_IdOrderByStartedAtDesc(Long userId);

    @Query(value = """
            SELECT s
            FROM Subscription s
            WHERE (
                :plan IS NULL
                OR s.plan = :plan
            )
            AND (
                :status IS NULL

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                    AND s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                    AND s.expiresAt > :now
                )

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.EXPIRED
                    AND (
                        s.status = com.readora.backend.enums.SubscriptionStatus.EXPIRED
                        OR (
                            s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                            AND s.expiresAt <= :now
                        )
                    )
                )

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.CANCELLED
                    AND s.status = com.readora.backend.enums.SubscriptionStatus.CANCELLED
                )
            )
            """, countQuery = """
            SELECT COUNT(s)
            FROM Subscription s
            WHERE (
                :plan IS NULL
                OR s.plan = :plan
            )
            AND (
                :status IS NULL

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                    AND s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                    AND s.expiresAt > :now
                )

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.EXPIRED
                    AND (
                        s.status = com.readora.backend.enums.SubscriptionStatus.EXPIRED
                        OR (
                            s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                            AND s.expiresAt <= :now
                        )
                    )
                )

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.CANCELLED
                    AND s.status = com.readora.backend.enums.SubscriptionStatus.CANCELLED
                )
            )
            """)
    Page<Subscription> findAdminSubscriptions(@Param("status") SubscriptionStatus status,
            @Param("plan") SubscriptionPlan plan, @Param("now") LocalDateTime now, Pageable pageable);

    @Query(value = """
            SELECT s
            FROM Subscription s
            WHERE (
                LOWER(s.user.name) LIKE :searchPattern
                OR LOWER(s.user.email) LIKE :searchPattern
            )
            AND (
                :plan IS NULL
                OR s.plan = :plan
            )
            AND (
                :status IS NULL

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                    AND s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                    AND s.expiresAt > :now
                )

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.EXPIRED
                    AND (
                        s.status = com.readora.backend.enums.SubscriptionStatus.EXPIRED
                        OR (
                            s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                            AND s.expiresAt <= :now
                        )
                    )
                )

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.CANCELLED
                    AND s.status = com.readora.backend.enums.SubscriptionStatus.CANCELLED
                )
            )
            """, countQuery = """
            SELECT COUNT(s)
            FROM Subscription s
            WHERE (
                LOWER(s.user.name) LIKE :searchPattern
                OR LOWER(s.user.email) LIKE :searchPattern
            )
            AND (
                :plan IS NULL
                OR s.plan = :plan
            )
            AND (
                :status IS NULL

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                    AND s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                    AND s.expiresAt > :now
                )

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.EXPIRED
                    AND (
                        s.status = com.readora.backend.enums.SubscriptionStatus.EXPIRED
                        OR (
                            s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                            AND s.expiresAt <= :now
                        )
                    )
                )

                OR (
                    :status = com.readora.backend.enums.SubscriptionStatus.CANCELLED
                    AND s.status = com.readora.backend.enums.SubscriptionStatus.CANCELLED
                )
            )
            """)
    Page<Subscription> findAdminSubscriptionsBySearch(@Param("searchPattern") String searchPattern,
            @Param("status") SubscriptionStatus status, @Param("plan") SubscriptionPlan plan,
            @Param("now") LocalDateTime now, Pageable pageable);

    long countByPlan(SubscriptionPlan plan);

    long countByStatus(SubscriptionStatus status);

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
            AND s.expiresAt > :now
            """)
    long countEffectiveActive(@Param("now") LocalDateTime now);

    @Query("""
            SELECT COUNT(s)
            FROM Subscription s
            WHERE s.status = com.readora.backend.enums.SubscriptionStatus.EXPIRED
            OR (
                s.status = com.readora.backend.enums.SubscriptionStatus.ACTIVE
                AND s.expiresAt <= :now
            )
            """)
    long countEffectiveExpired(@Param("now") LocalDateTime now);
}