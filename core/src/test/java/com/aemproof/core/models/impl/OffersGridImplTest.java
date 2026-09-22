package com.aemproof.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aemproof.core.integration.OfferApiClient;
import com.aemproof.core.integration.OfferDto;
import com.aemproof.core.models.Offer;
import com.aemproof.core.models.OffersGrid;
import com.aemproof.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AemContextExtension.class)
class OffersGridImplTest {

    private final AemContext context = AppAemContext.newAemContext();
    private final OfferApiClient api = mock(OfferApiClient.class);

    @BeforeEach
    void setUp() {
        context.registerService(OfferApiClient.class, api);
        context.addModelsForPackage("com.aemproof.core.models");
        context.load().json("/offers-grid/content.json", "/content/grid");
        context.load().json("/offers-grid/cf.json", "/content/dam/aemproof/offers");
    }

    private OffersGrid model(String node) {
        context.currentResource("/content/grid/" + node);
        return context.request().adaptTo(OffersGrid.class);
    }

    @Test
    void cfSourceSkipsExpiredAndNonFragmentsAndRespectsLimit() {
        OffersGrid g = model("cf");
        assertEquals(OffersGrid.SOURCE_CF, g.getSource());
        assertEquals(2, g.getLimit());
        List<Offer> offers = g.getOffers();
        assertEquals(2, offers.size());
        assertEquals("أ", offers.get(0).getTitle());
        assertEquals("12.00", offers.get(0).getPrice());
        assertEquals("SAR", offers.get(0).getCurrency());
        assertEquals("/content/dam/aemproof/offers/images/coffee.jpg", offers.get(0).getImage());
        assertEquals("2099-01-01", offers.get(0).getValidUntil());
        assertEquals(Arrays.asList("coffee", "daily"), offers.get(0).getTags());
        assertEquals("ج", offers.get(1).getTitle());
        assertEquals("5.50", offers.get(1).getPrice());
        assertEquals("", offers.get(1).getValidUntil());
        assertEquals(Arrays.asList("x", "y"), offers.get(1).getTags());
        assertFalse(g.isEmpty());
    }

    @Test
    void apiSourceDelegatesToService() {
        when(api.fetchOffers(3)).thenReturn(Collections.singletonList(
                new OfferDto("1", "x", "5.00", "SAR", "/i.jpg", "2099-01-01", Collections.singletonList("t"))));
        OffersGrid g = model("api");
        assertEquals(OffersGrid.SOURCE_API, g.getSource());
        assertEquals(1, g.getOffers().size());
        Offer o = g.getOffers().get(0);
        assertEquals("x", o.getTitle());
        assertEquals("5.00", o.getPrice());
        assertEquals("SAR", o.getCurrency());
        assertEquals("/i.jpg", o.getImage());
        assertEquals("2099-01-01", o.getValidUntil());
        assertEquals(Collections.singletonList("t"), o.getTags());
        verify(api).fetchOffers(3);
    }

    @Test
    void apiEmptyShowsFallback() {
        when(api.fetchOffers(anyInt())).thenReturn(Collections.emptyList());
        OffersGrid g = model("api");
        assertTrue(g.isEmpty());
        assertEquals("لا توجد عروض حالياً", g.getFallbackText());
    }

    @Test
    void defaultsAreSafe() {
        OffersGrid g = model("defaults");
        assertEquals(OffersGrid.SOURCE_CF, g.getSource());
        assertEquals(OffersGridImpl.DEFAULT_LIMIT, g.getLimit());
        assertTrue(g.isEmpty());
        assertNotNull(g.getFallbackText());
        assertEquals("", g.getFallbackText());
    }

    @Test
    void missingFolderIsEmptyNotError() {
        OffersGrid g = model("missingFolder");
        assertTrue(g.isEmpty());
    }
}
