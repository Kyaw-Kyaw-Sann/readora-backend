package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.AdminUserDetailResponse;
import com.readora.backend.dto.response.AdminUserSummaryResponse;
import com.readora.backend.entity.Subscription;
import com.readora.backend.entity.User;
import com.readora.backend.enums.SubscriptionStatus;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.AdminUserMapper;
import com.readora.backend.repository.SubscriptionRepository;
import com.readora.backend.repository.UserRepository;
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
public class AdminUserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    @Transactional
    public PageResponse<AdminUserSummaryResponse> getUsers(String search, Boolean verified, Boolean premium, int page,
            int size) {

        validatePagination(page, size);

        String normalizedSearch = normalizeSearch(search);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        LocalDateTime now = LocalDateTime.now();

        Page<User> userPage;

        if (normalizedSearch == null) {

            userPage = userRepository.findAdminUsers(verified, premium, SubscriptionStatus.ACTIVE, now, pageable);

        } else {

            String searchPattern = "%" + normalizedSearch + "%";

            userPage = userRepository.findAdminUsersBySearch(searchPattern, verified, premium,
                    SubscriptionStatus.ACTIVE, now, pageable);
        }

        Page<AdminUserSummaryResponse> responsePage = userPage.map(user -> {

            boolean premiumActive = subscriptionService.hasActiveSubscription(user.getId());

            Subscription latestSubscription = subscriptionRepository.findTopByUser_IdOrderByStartedAtDesc(user.getId())
                    .orElse(null);

            return AdminUserMapper.toSummaryResponse(user, premiumActive, latestSubscription);
        });

        return PageResponse.from(responsePage);
    }

    @Transactional
    public AdminUserDetailResponse getUserDetail(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean premiumActive = subscriptionService.hasActiveSubscription(user.getId());

        Subscription latestSubscription = subscriptionRepository.findTopByUser_IdOrderByStartedAtDesc(user.getId())
                .orElse(null);

        return AdminUserMapper.toDetailResponse(user, premiumActive, latestSubscription);
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