package com.aemproof.core.models;

import java.util.List;
import org.osgi.annotation.versioning.ConsumerType;

/** The offers-grid component: a bounded list of offers from Content Fragments or the offers API. */
@ConsumerType
public interface OffersGrid {

    String SOURCE_CF = "cf";
    String SOURCE_API = "api";

    List<Offer> getOffers();

    /** {@link #SOURCE_CF} or {@link #SOURCE_API}. */
    String getSource();

    int getLimit();

    boolean isEmpty();

    /** Authored text shown when there are no offers; empty string when not authored. */
    String getFallbackText();
}
