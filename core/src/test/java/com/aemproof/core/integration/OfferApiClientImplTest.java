package com.aemproof.core.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aemproof.core.testcontext.AppAemContext;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

@ExtendWith(AemContextExtension.class)
class OfferApiClientImplTest {

    private static final String PATH = "/offers?limit=3";

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance().build();

    private final AemContext context = AppAemContext.newAemContext();
    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-09-22T10:00:00Z"));
    private OfferApiClient client;

    @BeforeEach
    void setUp() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/wiremock/offers-ok.json")) {
            wm.stubFor(get(PATH).willReturn(okJson(new String(in.readAllBytes(), StandardCharsets.UTF_8))));
        }
        OfferApiClientImpl impl = new OfferApiClientImpl();
        impl.setClock(now::get);
        Map<String, Object> props = new HashMap<>();
        props.put("endpoint", wm.baseUrl() + "/offers");
        props.put("timeoutMs", 300);
        props.put("cacheTtlSeconds", 60);
        props.put("failureThreshold", 2);
        props.put("cooldownSeconds", 30);
        client = context.registerInjectActivateService(impl, props);
    }

    @Test
    void returnsParsedOffers() {
        List<OfferDto> offers = client.fetchOffers(3);
        assertEquals(3, offers.size());
        assertEquals("خصم ٢٠٪ على القهوة المختصة", offers.get(0).getTitle());
        assertEquals(Arrays.asList("coffee", "daily"), offers.get(0).getTags());
        assertEquals(OfferApiClient.State.CLOSED, client.status().getState());
        assertFalse(client.status().isServedFromCache());
        assertNotNull(client.status().getLastSuccess());
    }

    @Test
    void limitTruncatesResult() {
        wm.stubFor(get("/offers?limit=2").willReturn(okJson("{\"offers\":[{\"id\":\"a\"},{\"id\":\"b\"},{\"id\":\"c\"}]}")));
        assertEquals(2, client.fetchOffers(2).size());
    }

    @Test
    void serverErrorFallsBackToCache() {
        client.fetchOffers(3);
        wm.stubFor(get(PATH).willReturn(serverError()));
        assertEquals(3, client.fetchOffers(3).size());
        assertTrue(client.status().isServedFromCache());
        assertEquals(1, client.status().getConsecutiveFailures());
        assertNotNull(client.status().getLastFailure());
    }

    @Test
    void expiredCacheYieldsEmpty() {
        client.fetchOffers(3);
        wm.stubFor(get(PATH).willReturn(serverError()));
        now.set(now.get().plusSeconds(61));
        assertTrue(client.fetchOffers(3).isEmpty());
        assertFalse(client.status().isServedFromCache());
    }

    @Test
    void timeoutCountsAsFailure() {
        wm.stubFor(get(PATH).willReturn(okJson("{\"offers\":[]}").withFixedDelay(1000)));
        assertTrue(client.fetchOffers(3).isEmpty());
        assertEquals(1, client.status().getConsecutiveFailures());
    }

    @Test
    void malformedJsonIsFailureNotException() {
        wm.stubFor(get(PATH).willReturn(okJson("{not json")));
        assertDoesNotThrow(() -> client.fetchOffers(3));
        assertEquals(1, client.status().getConsecutiveFailures());
    }

    @Test
    void circuitOpensAndSkipsNetworkThenRecovers() throws Exception {
        wm.stubFor(get(PATH).willReturn(serverError()));
        client.fetchOffers(3);
        client.fetchOffers(3);
        assertEquals(OfferApiClient.State.OPEN, client.status().getState());
        client.fetchOffers(3);
        wm.verify(2, getRequestedFor(urlEqualTo(PATH)));

        now.set(now.get().plusSeconds(31));
        assertEquals(OfferApiClient.State.HALF_OPEN, client.status().getState());
        try (InputStream in = getClass().getResourceAsStream("/wiremock/offers-ok.json")) {
            wm.stubFor(get(PATH).willReturn(okJson(new String(in.readAllBytes(), StandardCharsets.UTF_8))));
        }
        assertEquals(3, client.fetchOffers(3).size());
        assertEquals(OfferApiClient.State.CLOSED, client.status().getState());
        assertEquals(0, client.status().getConsecutiveFailures());
    }

    @Test
    void parseToleratesMissingAndNullFields() {
        List<OfferDto> out = OfferApiClientImpl.parse("{\"offers\":[{\"id\":null,\"title\":\"x\",\"tags\":\"notarray\"}]}");
        assertEquals(1, out.size());
        assertEquals("", out.get(0).getId());
        assertEquals("x", out.get(0).getTitle());
        assertTrue(out.get(0).getTags().isEmpty());
        assertTrue(OfferApiClientImpl.parse("{}").isEmpty());
    }
}
