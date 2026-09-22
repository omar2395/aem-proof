package com.aemproof.core.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OfferApiClient} backed by {@link HttpClient}, with a hard timeout, an in-memory cache
 * that survives failures for {@code cacheTtlSeconds}, and a {@link CircuitBreaker} so a dead
 * API stops costing a timeout per page render.
 */
@Component(service = OfferApiClient.class, immediate = true)
@Designate(ocd = OfferApiClientConfig.class)
public class OfferApiClientImpl implements OfferApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(OfferApiClientImpl.class);

    private volatile OfferApiClientConfig config;
    private volatile HttpClient http;
    private volatile CircuitBreaker breaker;
    private Supplier<Instant> clock = Instant::now;

    private volatile List<OfferDto> cache = Collections.emptyList();
    private volatile Instant cachedAt;
    private volatile Instant lastSuccess;
    private volatile Instant lastFailure;
    private volatile boolean servedFromCache;

    @Activate
    @Modified
    protected void activate(OfferApiClientConfig config) {
        this.config = config;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(config.timeoutMs())).build();
        this.breaker = new CircuitBreaker(config.failureThreshold(), Duration.ofSeconds(config.cooldownSeconds()), clock);
        LOG.info("Offers API client configured: endpoint={} timeoutMs={} cacheTtl={}s threshold={} cooldown={}s",
                config.endpoint(), config.timeoutMs(), config.cacheTtlSeconds(), config.failureThreshold(), config.cooldownSeconds());
    }

    /** Test hook: inject a controllable clock before {@link #activate}. */
    void setClock(Supplier<Instant> clock) {
        this.clock = clock;
    }

    @Override
    public List<OfferDto> fetchOffers(int limit) {
        String cid = UUID.randomUUID().toString().substring(0, 8);
        if (!breaker.allowRequest()) {
            LOG.info("[{}] circuit OPEN, serving cache ({} items)", cid, cache.size());
            return cached(limit);
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(config.endpoint() + "?limit=" + limit))
                    .timeout(Duration.ofMillis(config.timeoutMs()))
                    .header("Accept", "application/json")
                    .header("X-Api-Key", config.apiKey())
                    .header("X-Correlation-Id", cid)
                    .GET()
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                throw new IOException("HTTP " + res.statusCode());
            }
            List<OfferDto> offers = parse(res.body());
            cache = offers;
            cachedAt = clock.get();
            lastSuccess = cachedAt;
            servedFromCache = false;
            breaker.onSuccess();
            LOG.debug("[{}] fetched {} offers", cid, offers.size());
            return head(offers, limit);
        } catch (IOException | RuntimeException e) {
            lastFailure = clock.get();
            breaker.onFailure();
            LOG.warn("[{}] offers API failed ({}), circuit {} after {} consecutive failure(s)",
                    cid, e.getMessage(), breaker.state(), breaker.consecutiveFailures());
            return cached(limit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return cached(limit);
        }
    }

    private List<OfferDto> cached(int limit) {
        boolean fresh = cachedAt != null
                && clock.get().isBefore(cachedAt.plusSeconds(config.cacheTtlSeconds()));
        servedFromCache = fresh && !cache.isEmpty();
        return servedFromCache ? head(cache, limit) : Collections.emptyList();
    }

    private static List<OfferDto> head(List<OfferDto> list, int limit) {
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    /** Parses {@code {"offers":[...]}}. Package-private for tests. */
    static List<OfferDto> parse(String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        JsonArray arr = root.has("offers") ? root.getAsJsonArray("offers") : new JsonArray();
        List<OfferDto> out = new ArrayList<>();
        for (JsonElement el : arr) {
            JsonObject o = el.getAsJsonObject();
            List<String> tags = new ArrayList<>();
            if (o.has("tags") && o.get("tags").isJsonArray()) {
                o.getAsJsonArray("tags").forEach(t -> tags.add(t.getAsString()));
            }
            out.add(new OfferDto(str(o, "id"), str(o, "title"), str(o, "price"), str(o, "currency"),
                    str(o, "imageUrl"), str(o, "validUntil"), tags));
        }
        return Collections.unmodifiableList(out);
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    @Override
    public Status status() {
        return new Status(breaker.state(), lastSuccess, lastFailure, breaker.consecutiveFailures(), servedFromCache);
    }
}
