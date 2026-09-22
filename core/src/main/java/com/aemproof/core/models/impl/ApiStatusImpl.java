package com.aemproof.core.models.impl;

import com.aemproof.core.integration.OfferApiClient;
import com.aemproof.core.models.ApiStatus;
import java.time.Instant;
import java.util.Locale;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = SlingHttpServletRequest.class,
       adapters = ApiStatus.class,
       resourceType = ApiStatusImpl.RESOURCE_TYPE,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ApiStatusImpl implements ApiStatus {

    static final String RESOURCE_TYPE = "aemproof/components/api-status";

    @OSGiService
    private OfferApiClient api;

    @ValueMapValue
    private String label;

    private OfferApiClient.Status status() {
        // No service bound (e.g. bundle stopped) reads as an open circuit: nothing is being fetched.
        return api == null
                ? new OfferApiClient.Status(OfferApiClient.State.OPEN, null, null, 0, false)
                : api.status();
    }

    private static String iso(Instant instant) {
        return instant == null ? "" : instant.toString();
    }

    @Override
    public String getState() {
        return status().getState().name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    @Override
    public String getStateKey() {
        return "apistatus." + getState();
    }

    @Override
    public String getLastSuccess() {
        return iso(status().getLastSuccess());
    }

    @Override
    public String getLastFailure() {
        return iso(status().getLastFailure());
    }

    @Override
    public int getConsecutiveFailures() {
        return status().getConsecutiveFailures();
    }

    @Override
    public boolean isServedFromCache() {
        return status().isServedFromCache();
    }

    @Override
    public String getLabel() {
        return label == null ? "" : label;
    }
}
