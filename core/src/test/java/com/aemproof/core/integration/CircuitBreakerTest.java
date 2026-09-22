package com.aemproof.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest {

    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-09-22T10:00:00Z"));
    private final CircuitBreaker cb = new CircuitBreaker(3, Duration.ofSeconds(30), now::get);

    @Test
    void closedUntilThresholdFailures() {
        cb.onFailure();
        cb.onFailure();
        assertEquals(OfferApiClient.State.CLOSED, cb.state());
        assertTrue(cb.allowRequest());
        cb.onFailure();
        assertEquals(OfferApiClient.State.OPEN, cb.state());
        assertFalse(cb.allowRequest());
    }

    @Test
    void halfOpenAfterCooldownThenClosesOnSuccess() {
        cb.onFailure();
        cb.onFailure();
        cb.onFailure();
        now.set(now.get().plusSeconds(31));
        assertEquals(OfferApiClient.State.HALF_OPEN, cb.state());
        assertTrue(cb.allowRequest());
        cb.onSuccess();
        assertEquals(OfferApiClient.State.CLOSED, cb.state());
        assertEquals(0, cb.consecutiveFailures());
    }

    @Test
    void halfOpenFailureReopens() {
        cb.onFailure();
        cb.onFailure();
        cb.onFailure();
        now.set(now.get().plusSeconds(31));
        cb.onFailure();
        assertEquals(OfferApiClient.State.OPEN, cb.state());
        assertFalse(cb.allowRequest());
    }
}
