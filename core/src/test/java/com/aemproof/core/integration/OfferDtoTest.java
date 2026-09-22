package com.aemproof.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class OfferDtoTest {

    private static OfferDto sample() {
        return new OfferDto("o1", "t", "1.00", "SAR", "/i.jpg", "2026-12-31", Arrays.asList("a", "b"));
    }

    @Test
    void nullsBecomeEmptyAndTagsAreImmutable() {
        OfferDto d = new OfferDto(null, null, null, null, null, null, null);
        assertEquals("", d.getId());
        assertEquals("", d.getTitle());
        assertEquals("", d.getPrice());
        assertEquals("", d.getCurrency());
        assertEquals("", d.getImageUrl());
        assertEquals("", d.getValidUntil());
        assertTrue(d.getTags().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> sample().getTags().add("x"));
    }

    @Test
    void valueEquality() {
        assertEquals(sample(), sample());
        assertEquals(sample().hashCode(), sample().hashCode());
        assertNotEquals(sample(), new OfferDto("o2", "t", "1.00", "SAR", "/i.jpg", "2026-12-31", Arrays.asList("a", "b")));
        assertNotEquals(sample(), new OfferDto("o1", "t", "1.00", "SAR", "/i.jpg", "2026-12-31", Collections.emptyList()));
        assertNotEquals(sample(), "not an offer");
        assertTrue(sample().toString().contains("o1"));
    }
}
