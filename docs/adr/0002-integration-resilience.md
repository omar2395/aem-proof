# ADR 0002 — Pages never depend directly on the external API

Status: accepted · 2026-09-22

## Context
An authored page that calls a REST API at render time fails the moment that API is slow or down,
and it fails on author, publish and in CI alike.

## Decision
All external calls go through one OSGi service (`OfferApiClient`). It has a hard timeout, an
in-memory cache with TTL, and a circuit breaker (CLOSED / OPEN / HALF_OPEN). It never throws:
callers get fresh data, cached data, or an empty list, and can ask `status()` to explain which.
Configuration is per run mode in `ui.config`; the API key is a Cloud Manager secret environment
variable referenced as `$[secret:AEMPROOF_OFFERS_API_KEY]`.

## Consequences
- Components render a fallback state instead of a 500, and the `api-status` component shows why.
- Behaviour is fully unit-tested with WireMock (`OfferApiClientImplTest`): success, limit, HTTP 500,
  expired cache, timeout, malformed JSON, circuit open → half-open → closed.
- `java.net.http.HttpClient` (JDK 11) is used instead of Apache HttpClient to avoid an extra
  dependency. Swap to `HttpClientBuilderFactory` if proxy configuration from OSGi becomes a need.
- The AEM SDK API jar contains Gson as API stubs only, so Gson is a test-scoped dependency in
  `core/pom.xml`; AEM provides the real bundle at runtime.
