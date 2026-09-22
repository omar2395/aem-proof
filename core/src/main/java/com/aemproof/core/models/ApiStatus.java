package com.aemproof.core.models;

import org.osgi.annotation.versioning.ConsumerType;

/** The api-status component: a read-only view of the offers API circuit breaker. */
@ConsumerType
public interface ApiStatus {

    /** {@code closed}, {@code open} or {@code half-open}; doubles as the CSS modifier. */
    String getState();

    /** i18n key for the state label, e.g. {@code apistatus.half-open}. */
    String getStateKey();

    /** ISO-8601 instant or empty. */
    String getLastSuccess();

    /** ISO-8601 instant or empty. */
    String getLastFailure();

    int getConsecutiveFailures();

    boolean isServedFromCache();

    /** Authored label; empty when not authored. */
    String getLabel();
}
