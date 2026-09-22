package com.aemproof.core.integration;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable offer as returned by the external offers API. */
public final class OfferDto {
    private final String id;
    private final String title;
    private final String price;
    private final String currency;
    private final String imageUrl;
    private final String validUntil;
    private final List<String> tags;

    public OfferDto(String id, String title, String price, String currency,
                    String imageUrl, String validUntil, List<String> tags) {
        this.id = nz(id);
        this.title = nz(title);
        this.price = nz(price);
        this.currency = nz(currency);
        this.imageUrl = nz(imageUrl);
        this.validUntil = nz(validUntil);
        this.tags = tags == null ? Collections.emptyList() : Collections.unmodifiableList(tags);
    }

    private static String nz(String s) { return s == null ? "" : s; }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getCurrency() { return currency; }
    public String getImageUrl() { return imageUrl; }
    public String getValidUntil() { return validUntil; }
    public List<String> getTags() { return tags; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfferDto)) return false;
        OfferDto d = (OfferDto) o;
        return id.equals(d.id) && title.equals(d.title) && price.equals(d.price) && currency.equals(d.currency)
                && imageUrl.equals(d.imageUrl) && validUntil.equals(d.validUntil) && tags.equals(d.tags);
    }
    @Override public int hashCode() { return Objects.hash(id, title, price, currency, imageUrl, validUntil, tags); }
    @Override public String toString() { return "OfferDto{" + id + ", " + title + ", " + price + " " + currency + "}"; }
}
