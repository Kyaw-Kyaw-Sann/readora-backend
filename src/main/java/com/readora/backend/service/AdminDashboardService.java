package com.readora.backend.service;

import com.readora.backend.dto.response.AdminDashboardBookStatsResponse;
import com.readora.backend.dto.response.AdminDashboardResponse;
import com.readora.backend.dto.response.AdminDashboardReviewStatsResponse;
import com.readora.backend.dto.response.AdminDashboardSubscriptionStatsResponse;
import com.readora.backend.dto.response.AdminDashboardUserStatsResponse;
import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.enums.SubscriptionPlan;
import com.readora.backend.enums.SubscriptionStatus;
import com.readora.backend.repository.BookRepository;
import com.readora.backend.repository.ReviewRepository;
import com.readora.backend.repository.SubscriptionRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {

        LocalDateTime now = LocalDateTime.now();

        AdminDashboardUserStatsResponse userStats = buildUserStats(now);

        AdminDashboardBookStatsResponse bookStats = buildBookStats();

        AdminDashboardReviewStatsResponse reviewStats = buildReviewStats();

        AdminDashboardSubscriptionStatsResponse subscriptionStats = buildSubscriptionStats(now);

        return new AdminDashboardResponse(userStats, bookStats, reviewStats, subscriptionStats);
    }

    private AdminDashboardUserStatsResponse buildUserStats(LocalDateTime now) {

        long totalUsers = userRepository.count();

        long verifiedUsers = userRepository.countByEmailVerifiedTrue();

        long unverifiedUsers = userRepository.countByEmailVerifiedFalse();

        long activePremiumUsers = subscriptionRepository.countEffectiveActive(now);

        return new AdminDashboardUserStatsResponse(totalUsers, verifiedUsers, unverifiedUsers, activePremiumUsers);
    }

    private AdminDashboardBookStatsResponse buildBookStats() {

        long totalBooks = bookRepository.count();

        long publishedBooks = bookRepository.countByStatus(BookStatus.PUBLISHED);

        long draftBooks = bookRepository.countByStatus(BookStatus.DRAFT);

        long archivedBooks = bookRepository.countByStatus(BookStatus.ARCHIVED);

        long freeBooks = bookRepository.countByAccessType(BookAccessType.FREE);

        long premiumBooks = bookRepository.countByAccessType(BookAccessType.PREMIUM);

        return new AdminDashboardBookStatsResponse(totalBooks, publishedBooks, draftBooks, archivedBooks, freeBooks,
                premiumBooks);
    }

    private AdminDashboardReviewStatsResponse buildReviewStats() {

        long totalReviews = reviewRepository.count();

        Double average = reviewRepository.findGlobalAverageRating();

        double averageRating = average == null ? 0.0 : roundToOneDecimal(average);

        return new AdminDashboardReviewStatsResponse(totalReviews, averageRating);
    }

    private AdminDashboardSubscriptionStatsResponse buildSubscriptionStats(LocalDateTime now) {

        long totalSubscriptions = subscriptionRepository.count();

        long activeSubscriptions = subscriptionRepository.countEffectiveActive(now);

        long expiredSubscriptions = subscriptionRepository.countEffectiveExpired(now);

        long cancelledSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);

        long monthlySubscriptions = subscriptionRepository.countByPlan(SubscriptionPlan.MONTHLY);

        long yearlySubscriptions = subscriptionRepository.countByPlan(SubscriptionPlan.YEARLY);

        return new AdminDashboardSubscriptionStatsResponse(totalSubscriptions, activeSubscriptions,
                expiredSubscriptions, cancelledSubscriptions, monthlySubscriptions, yearlySubscriptions);
    }

    private double roundToOneDecimal(double value) {

        return Math.round(value * 10.0) / 10.0;
    }
}