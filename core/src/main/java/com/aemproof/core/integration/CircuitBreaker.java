package com.aemproof.core.integration;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Minimal circuit breaker: CLOSED, then OPEN after {@code threshold} consecutive failures,
 * HALF_OPEN once the cooldown has passed. A success in any state closes it again; a failure
 * while half-open reopens it immediately.
 */
final class CircuitBreaker {
    private final int threshold;
    private final Duration cooldown;
    private final Supplier<Instant> clock;
    private int failures;
    private Instant openedAt;

    CircuitBreaker(int threshold, Duration cooldown, Supplier<Instant> clock) {
        this.threshold = threshold;
        this.cooldown = cooldown;
        this.clock = clock;
    }

    synchronized OfferApiClient.State state() {
        if (openedAt == null) {
            return OfferApiClient.State.CLOSED;
        }
        return clock.get().isAfter(openedAt.plus(cooldown))
                ? OfferApiClient.State.HALF_OPEN
                : OfferApiClient.State.OPEN;
    }

    synchronized boolean allowRequest() {
        return state() != OfferApiClient.State.OPEN;
    }

    synchronized void onSuccess() {
        failures = 0;
        openedAt = null;
    }

    synchronized void onFailure() {
        boolean wasHalfOpen = state() == OfferApiClient.State.HALF_OPEN;
        failures++;
        if (failures >= threshold || wasHalfOpen) {
            openedAt = clock.get();
        }
    }

    synchronized int consecutiveFailures() {
        return failures;
    }
}
