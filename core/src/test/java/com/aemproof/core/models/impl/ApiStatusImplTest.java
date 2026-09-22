package com.aemproof.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aemproof.core.integration.OfferApiClient;
import com.aemproof.core.models.ApiStatus;
import com.aemproof.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AemContextExtension.class)
class ApiStatusImplTest {

    private final AemContext context = AppAemContext.newAemContext();

    private ApiStatus adapt(String label) {
        context.addModelsForPackage("com.aemproof.core.models");
        if (label == null) {
            context.create().resource("/content/s", "sling:resourceType", ApiStatusImpl.RESOURCE_TYPE);
        } else {
            context.create().resource("/content/s", "sling:resourceType", ApiStatusImpl.RESOURCE_TYPE, "label", label);
        }
        context.currentResource("/content/s");
        return context.request().adaptTo(ApiStatus.class);
    }

    @Test
    void exposesServiceStatus() {
        OfferApiClient api = mock(OfferApiClient.class);
        when(api.status()).thenReturn(new OfferApiClient.Status(OfferApiClient.State.HALF_OPEN,
                Instant.parse("2026-09-22T10:00:00Z"), null, 3, true));
        context.registerService(OfferApiClient.class, api);

        ApiStatus s = adapt("حالة الربط");
        assertEquals("half-open", s.getState());
        assertEquals("apistatus.half-open", s.getStateKey());
        assertEquals("2026-09-22T10:00:00Z", s.getLastSuccess());
        assertEquals("", s.getLastFailure());
        assertEquals(3, s.getConsecutiveFailures());
        assertTrue(s.isServedFromCache());
        assertEquals("حالة الربط", s.getLabel());
    }

    @Test
    void noServiceReadsAsOpenWithEmptyLabel() {
        ApiStatus s = adapt(null);
        assertEquals("open", s.getState());
        assertEquals("", s.getLastSuccess());
        assertEquals(0, s.getConsecutiveFailures());
        assertFalse(s.isServedFromCache());
        assertEquals("", s.getLabel());
    }
}
