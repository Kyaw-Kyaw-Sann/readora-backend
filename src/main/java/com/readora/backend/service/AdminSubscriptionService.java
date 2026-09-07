package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.AdminSubscriptionResponse;
import com.readora.backend.entity.Subscription;
import com.readora.backend.enums.SubscriptionPlan;
import com.readora.backend.enums.SubscriptionStatus;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.mapper.AdminSubscriptionMapper;
import com.readora.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public PageResponse<AdminSubscriptionResponse> getSubscriptions(String search, SubscriptionStatus status,
            SubscriptionPlan plan, int page, int size) {

        validatePagination(page, size);

        String normalizedSearch = normalizeSearch(search);

        LocalDateTime now = LocalDateTime.now();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));

        Page<Subscription> subscriptionPage;

        if (normalizedSearch == null) {

            subscriptionPage = findSubscriptionsWithoutSearch(status, plan, now, pageable);

        } else {

            String searchPattern = "%" + normalizedSearch + "%";

            subscriptionPage = findSubscriptionsBySearch(searchPattern, status, plan, now, pageable);
        }

        Page<AdminSubscriptionResponse> responsePage = subscriptionPage
                .map(subscription -> AdminSubscriptionMapper.toResponse(subscription, now));

        return PageResponse.from(responsePage);
    }

    private Page<Subscription> findSubscriptionsWithoutSearch(SubscriptionStatus status, SubscriptionPlan plan,
            LocalDateTime now, Pageable pageable) {

        if (status == null) {
            return subscriptionRepository.findAdminSubscriptions(plan, pageable);
        }

        return switch (status) {
        case ACTIVE -> subscriptionRepository.findAdminActiveSubscriptions(plan, now, pageable);
        case EXPIRED -> subscriptionRepository.findAdminExpiredSubscriptions(plan, now, pageable);
        case CANCELLED -> subscriptionRepository.findAdminCancelledSubscriptions(plan, pageable);
        };
    }

    private Page<Subscription> findSubscriptionsBySearch(String searchPattern, SubscriptionStatus status,
            SubscriptionPlan plan, LocalDateTime now, Pageable pageable) {

        if (status == null) {
            return subscriptionRepository.findAdminSubscriptionsBySearch(searchPattern, plan, pageable);
        }

        return switch (status) {
        case ACTIVE -> subscriptionRepository.findAdminActiveSubscriptionsBySearch(searchPattern, plan, now,
                pageable);
        case EXPIRED -> subscriptionRepository.findAdminExpiredSubscriptionsBySearch(searchPattern, plan, now,
                pageable);
        case CANCELLED -> subscriptionRepository.findAdminCancelledSubscriptionsBySearch(searchPattern, plan,
                pageable);
        };
    }

    private String normalizeSearch(String search) {

        if (search == null || search.isBlank()) {

            return null;
        }

        return search.trim().toLowerCase();
    }

    private void validatePagination(int page, int size) {

        if (page < 0) {
            throw new BadRequestException("Page number must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }
}
