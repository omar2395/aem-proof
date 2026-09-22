# ADR 0003 — Offers are Content Fragments; the API is the second source

Status: accepted · 2026-09-22

Editors own offers in the DAM as Content Fragments (`offer` model, Arabic master + `en`
variation), so marketing can publish an offer with no deploy. The same grid can instead read the
external API (ADR 0002) when the source is a live system. The Sling Model normalises both into one
`Offer` interface so the HTL never knows where an offer came from.

Expired fragments (`validUntil` in the past) are filtered by the model, not by the author.
