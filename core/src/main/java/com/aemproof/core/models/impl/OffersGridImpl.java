package com.aemproof.core.models.impl;

import com.aemproof.core.integration.OfferApiClient;
import com.aemproof.core.integration.OfferDto;
import com.aemproof.core.models.Offer;
import com.aemproof.core.models.OffersGrid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = SlingHttpServletRequest.class,
       adapters = OffersGrid.class,
       resourceType = OffersGridImpl.RESOURCE_TYPE,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = "jackson", extensions = "json")
public class OffersGridImpl implements OffersGrid {

    static final String RESOURCE_TYPE = "aemproof/components/offers-grid";
    static final int DEFAULT_LIMIT = 6;

    @ValueMapValue
    private String source;

    @ValueMapValue
    private String cfFolder;

    @ValueMapValue
    private int limit;

    @ValueMapValue
    private String fallbackText;

    @OSGiService
    private OfferApiClient api;

    @SlingObject
    private ResourceResolver resolver;

    private List<Offer> offers;

    @PostConstruct
    protected void init() {
        if (!SOURCE_API.equals(source)) {
            source = SOURCE_CF;
        }
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        if (fallbackText == null) {
            fallbackText = "";
        }
        offers = Collections.unmodifiableList(SOURCE_API.equals(source) ? fromApi() : fromFragments());
    }

    private List<Offer> fromApi() {
        List<Offer> out = new ArrayList<>();
        if (api == null) {
            return out;
        }
        for (OfferDto dto : api.fetchOffers(limit)) {
            out.add(new OfferDtoAdapter(dto));
        }
        return out;
    }

    private List<Offer> fromFragments() {
        List<Offer> out = new ArrayList<>();
        Resource folder = cfFolder == null ? null : resolver.getResource(cfFolder);
        if (folder == null) {
            return out;
        }
        LocalDate today = LocalDate.now();
        for (Resource child : folder.getChildren()) {
            if (out.size() >= limit) {
                break;
            }
            OfferCfModel.from(child, OfferCfModel.MASTER).filter(o -> o.isValid(today)).ifPresent(out::add);
        }
        return out;
    }

    @Override
    public List<Offer> getOffers() {
        return offers;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public boolean isEmpty() {
        return offers.isEmpty();
    }

    @Override
    public String getFallbackText() {
        return fallbackText;
    }

    /** Adapts the integration DTO to the view interface. */
    static final class OfferDtoAdapter implements Offer {
        private final OfferDto dto;

        OfferDtoAdapter(OfferDto dto) {
            this.dto = dto;
        }

        @Override public String getTitle() { return dto.getTitle(); }
        @Override public String getPrice() { return dto.getPrice(); }
        @Override public String getCurrency() { return dto.getCurrency(); }
        @Override public String getImage() { return dto.getImageUrl(); }
        @Override public String getValidUntil() { return dto.getValidUntil(); }
        @Override public List<String> getTags() { return dto.getTags(); }
    }
}
