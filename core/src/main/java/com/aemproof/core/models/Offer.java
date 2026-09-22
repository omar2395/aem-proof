package com.aemproof.core.models;

import java.util.List;
import org.osgi.annotation.versioning.ConsumerType;

/** One offer as the HTL sees it, regardless of where it came from (Content Fragment or API). */
@ConsumerType
public interface Offer {

    String getTitle();

    /** Always formatted with two decimals, e.g. {@code "12.00"}; empty when unknown. */
    String getPrice();

    String getCurrency();

    /** DAM path or absolute URL; empty when the offer has no image. */
    String getImage();

    /** ISO date ({@code yyyy-MM-dd}) or empty when the offer does not expire. */
    String getValidUntil();

    List<String> getTags();
}
