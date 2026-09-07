package com.readora.backend.dto.request;

import com.readora.backend.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionRequest(

        @NotNull(message = "Subscription plan is required") SubscriptionPlan plan

) {
}