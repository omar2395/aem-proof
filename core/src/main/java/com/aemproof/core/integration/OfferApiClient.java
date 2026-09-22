package com.aemproof.core.integration;

import java.time.Instant;
import java.util.List;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Reads offers from the external offers API.
 * <p>Never throws to callers: the result is fresh data, cached data, or an empty list, and
 * {@link #status()} explains which.</p>
 */
@ProviderType
public interface OfferApiClient {

    /** Circuit breaker state. */
    enum State { CLOSED, OPEN, HALF_OPEN }

    /** Snapshot of the integration's health, for the api-status component and for logs. */
    final class Status {
        private final State state;
        private final Instant lastSuccess;
        private final Instant lastFailure;
        private final int consecutiveFailures;
        private final boolean servedFromCache;

        public Status(State state, Instant lastSuccess, Instant lastFailure, int consecutiveFailures, boolean servedFromCache) {
            this.state = state;
            this.lastSuccess = lastSuccess;
            this.lastFailure = lastFailure;
            this.consecutiveFailures = consecutiveFailures;
            this.servedFromCache = servedFromCache;
        }
        public State getState() { return state; }
        public Instant getLastSuccess() { return lastSuccess; }
        public Instant getLastFailure() { return lastFailure; }
        public int getConsecutiveFailures() { return consecutiveFailures; }
        public boolean isServedFromCache() { return servedFromCache; }
    }

    /** @return up to {@code limit} offers; cached or empty when the API is unavailable. */
    List<OfferDto> fetchOffers(int limit);

    Status status();
}
