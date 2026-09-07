package com.readora.backend.service;

import com.readora.backend.dto.request.CreateSubscriptionRequest;
import com.readora.backend.dto.response.SubscriptionResponse;
import com.readora.backend.entity.Subscription;
import com.readora.backend.entity.User;
import com.readora.backend.enums.SubscriptionPlan;
import com.readora.backend.enums.SubscriptionStatus;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.SubscriptionMapper;
import com.readora.backend.repository.SubscriptionRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional
    public SubscriptionResponse getCurrentSubscription(String email) {
        User user = getUserByEmail(email);

        Optional<Subscription> activeSubscription = getActiveSubscription(user.getId());

        return activeSubscription.map(SubscriptionMapper::toResponse).orElse(null);
    }

    @Transactional
    public SubscriptionResponse createSubscription(String email, CreateSubscriptionRequest request) {

        User user = getUserByEmail(email);

        Optional<Subscription> existingActiveSubscription = getActiveSubscription(user.getId());

        if (existingActiveSubscription.isPresent()) {
            throw new BadRequestException("You already have an active subscription");
        }

        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime expiresAt = calculateExpirationDate(startedAt, request.plan());

        Subscription subscription = Subscription.builder().user(user).plan(request.plan())
                .status(SubscriptionStatus.ACTIVE).startedAt(startedAt).expiresAt(expiresAt).cancelledAt(null).build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return SubscriptionMapper.toResponse(savedSubscription);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(String email) {
        User user = getUserByEmail(email);

        Subscription subscription = getActiveSubscription(user.getId())
                .orElseThrow(() -> new BadRequestException("No active subscription to cancel"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return SubscriptionMapper.toResponse(savedSubscription);
    }

    @Transactional
    public List<SubscriptionResponse> getSubscriptionHistory(String email) {
        User user = getUserByEmail(email);

        expireActiveSubscriptionIfNeeded(user.getId());

        return subscriptionRepository.findAllByUser_IdOrderByStartedAtDesc(user.getId()).stream()
                .map(SubscriptionMapper::toResponse).toList();
    }

    @Transactional
    public boolean hasActiveSubscription(Long userId) {
        return getActiveSubscription(userId).isPresent();
    }

    @Transactional
    public String getSubscriptionStatus(Long userId) {
        expireActiveSubscriptionIfNeeded(userId);

        return subscriptionRepository.findTopByUser_IdOrderByStartedAtDesc(userId)
                .map(subscription -> subscription.getStatus().name()).orElse("NONE");
    }

    private Optional<Subscription> getActiveSubscription(Long userId) {
        Optional<Subscription> activeSubscription = subscriptionRepository
                .findTopByUser_IdAndStatusOrderByStartedAtDesc(userId, SubscriptionStatus.ACTIVE);

        if (activeSubscription.isEmpty()) {
            return Optional.empty();
        }

        Subscription subscription = activeSubscription.get();

        if (isExpired(subscription)) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);

            return Optional.empty();
        }

        return Optional.of(subscription);
    }

    private void expireActiveSubscriptionIfNeeded(Long userId) {
        Optional<Subscription> activeSubscription = subscriptionRepository
                .findTopByUser_IdAndStatusOrderByStartedAtDesc(userId, SubscriptionStatus.ACTIVE);

        if (activeSubscription.isPresent() && isExpired(activeSubscription.get())) {

            Subscription subscription = activeSubscription.get();
            subscription.setStatus(SubscriptionStatus.EXPIRED);

            subscriptionRepository.save(subscription);
        }
    }

    private boolean isExpired(Subscription subscription) {
        return !subscription.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private LocalDateTime calculateExpirationDate(LocalDateTime startedAt, SubscriptionPlan plan) {

        return switch (plan) {
        case MONTHLY -> startedAt.plusMonths(1);
        case YEARLY -> startedAt.plusYears(1);
        };
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}