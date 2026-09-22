package com.aemproof.core.models.impl;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.aemproof.core.models.Offer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

/**
 * Adapts a structured Content Fragment of the {@code offer} model to {@link Offer}.
 * <p>
 * The fragment is validated through the Content Fragment API ({@link ContentFragment}), and its
 * values are read from the variation node ({@code jcr:content/data/<variation>}), which is the
 * typed storage the CF API itself reads: dates come back as {@link Calendar}, multifields as
 * {@code String[]}. Not a Sling Model itself: {@link OffersGridImpl} builds one per fragment.
 */
final class OfferCfModel implements Offer {

    static final String MODEL_PATH = "/conf/aemproof/settings/dam/cfm/models/offer";
    static final String MASTER = "master";

    private final ValueMap values;

    private OfferCfModel(ValueMap values) {
        this.values = values;
    }

    /**
     * @param resource  a {@code dam:Asset}
     * @param variation variation name, {@code master} for the default
     * @return the adapted offer, or empty if the resource is not an offer fragment
     */
    static Optional<OfferCfModel> from(Resource resource, String variation) {
        if (resource == null || resource.adaptTo(ContentFragment.class) == null) {
            return Optional.empty();
        }
        Resource data = resource.getChild("jcr:content/data");
        if (data == null || !MODEL_PATH.equals(data.getValueMap().get("cq:model", String.class))) {
            return Optional.empty();
        }
        Resource node = data.getChild(variation);
        if (node == null) {
            node = data.getChild(MASTER);
        }
        return node == null ? Optional.empty() : Optional.of(new OfferCfModel(node.getValueMap()));
    }

    private String text(String name) {
        return values.get(name, "");
    }

    @Override
    public String getTitle() {
        return text("title");
    }

    @Override
    public String getPrice() {
        String raw = text("price");
        if (raw.isEmpty()) {
            return "";
        }
        try {
            return new BigDecimal(raw).setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (NumberFormatException e) {
            return raw;
        }
    }

    @Override
    public String getCurrency() {
        return text("currency");
    }

    @Override
    public String getImage() {
        return text("image");
    }

    @Override
    public String getValidUntil() {
        Calendar c = values.get("validUntil", Calendar.class);
        if (c != null) {
            return c.toInstant().atZone(c.getTimeZone().toZoneId()).toLocalDate().toString();
        }
        String s = text("validUntil");
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    @Override
    public List<String> getTags() {
        // Multifield storage is String[]; a hand-edited or migrated fragment may hold "a, b" in one value.
        String[] arr = values.get("tags", String[].class);
        if (arr == null) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        for (String value : arr) {
            for (String t : value.split(",")) {
                if (!t.trim().isEmpty()) {
                    out.add(t.trim());
                }
            }
        }
        return Collections.unmodifiableList(out);
    }

    /** @return false only when {@code validUntil} is a parseable date before {@code today}. */
    boolean isValid(LocalDate today) {
        String until = getValidUntil();
        if (until.isEmpty()) {
            return true;
        }
        try {
            return !LocalDate.parse(until).isBefore(today);
        } catch (DateTimeParseException e) {
            return true;
        }
    }
}
