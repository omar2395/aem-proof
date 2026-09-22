package com.aemproof.core.integration;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "AEM Proof - Offers API client",
        description = "External offers REST API. In Cloud Manager the API key is the AEMPROOF_OFFERS_API_KEY secret environment variable.")
public @interface OfferApiClientConfig {

    @AttributeDefinition(name = "Endpoint", description = "Base URL of the offers resource, without query string.")
    String endpoint() default "https://api.example.com/offers";

    @AttributeDefinition(name = "Timeout (ms)", description = "Connect and read timeout for one request.")
    int timeoutMs() default 1500;

    @AttributeDefinition(name = "Cache TTL (s)", description = "How long the last successful response may be served after a failure.")
    int cacheTtlSeconds() default 300;

    @AttributeDefinition(name = "Failures before the circuit opens")
    int failureThreshold() default 3;

    @AttributeDefinition(name = "Cooldown before half-open (s)", description = "While open, no request leaves AEM. After the cooldown one probe request is allowed.")
    int cooldownSeconds() default 60;

    @AttributeDefinition(name = "API key", type = AttributeType.PASSWORD)
    String apiKey() default "";
}
