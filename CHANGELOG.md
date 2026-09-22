# Changelog

## 1.0.0 — 2026-09-22
- Generated from AEM Project Archetype 58 (cloud); samples and the Cypress `ui.tests` module removed.
- Documentation skeleton and CI.
- `OfferApiClient` OSGi service: timeout, cache, circuit breaker, per-run-mode config (ADR-0002).
- `offer` Content Fragment model with five Arabic/English fragments (ADR-0003).
- `OffersGrid` Sling Model reading Content Fragments or the offers API, exported as JSON.
- `offers-grid` and `api-status` components: dialogs, HTL, `ApiStatus` Sling Model.
- Header Experience Fragment content, Arabic/English i18n dictionary, Arabic offers home page (ADR-0004).
- Front-end design system in `ui.frontend`: tokens, self-hosted Arabic/Latin fonts, offers grid with hero card and price tag, API status instrument, RTL by language; static design preview.
- Dispatcher filter and cache rules for offers pages and `model.json`.
- JaCoCo 80 % line-coverage gate on `core` (currently 96.9 %).
- Five sample product photographs in the DAM, referenced by the offer fragments.
- Arabic README, architecture request flow and requirement map, static preview build script.
