# Architecture

## Modules

| Module | Responsibility |
|---|---|
| `core` | Java: Sling Models (`com.aemproof.core.models`) and OSGi services (`com.aemproof.core.integration`). |
| `ui.apps` | Components (dialog XML + HTL), client libraries, i18n dictionary. |
| `ui.content` | Templates, policies, Content Fragment model, Experience Fragments, sample content. |
| `ui.config` | OSGi configuration per run mode (`config`, `config.dev`, `config.prod`, `config.author`, `config.publish`). |
| `ui.frontend` | Webpack build producing `clientlib-site`; also hosts the static design preview. |
| `ui.apps.structure` | Repository structure package (Cloud Service requirement). |
| `it.tests` | Integration tests run against a real instance by Cloud Manager. |
| `dispatcher` | Cloud Service dispatcher configuration. |
| `all` | The single deployable content package; the AEM Cloud Service analyser runs here on every `mvn verify`. |

## Request flow

```
Browser
  │
  ▼
Dispatcher   cache: /content/aemproof/*.html allowed · *.model.json denied
  │          filter: GET *.model.json allowed (Sling Model exporter)
  ▼
Publish      /content/aemproof/sa/ar.html
  │
  ├─ template page-content (structure, locked)
  │    ├─ experiencefragment-header ──▶ /content/experience-fragments/aemproof/sa/ar/site/header/master
  │    ├─ container (editable)
  │    │    ├─ title
  │    │    ├─ offers-grid ──▶ OffersGrid (Sling Model, request-adaptable, JSON exporter)
  │    │    │                    ├─ source=cf  ──▶ OfferCfModel ◀── /content/dam/aemproof/offers/* (CF model "offer")
  │    │    │                    └─ source=api ──▶ OfferApiClient (OSGi)
  │    │    │                                        ├─ circuit breaker  CLOSED → OPEN → HALF_OPEN
  │    │    │                                        ├─ in-memory cache  (TTL per run mode)
  │    │    │                                        └─ HttpClient, hard timeout ──▶ external REST API
  │    │    └─ api-status ──▶ ApiStatus (Sling Model) ◀── OfferApiClient.status()
  │    └─ experiencefragment-footer
  │
  └─ clientlib aemproof.site  (ui.frontend → ui.apps/…/clientlibs/clientlib-site)
```

The HTL of `offers-grid` never learns where an offer came from. `OffersGrid` normalises both
sources into the `Offer` interface; the integration service never throws, so the page renders a
fallback state rather than a 500 when the API is slow or down (ADR-0002).

## Where each requirement from the brief lives

| Requirement | Where to look |
|---|---|
| AEM component development | `ui.apps/…/components/offers-grid`, `api-status` |
| Custom components in HTL / Sightly | `offers-grid.html`, `card.html`, `api-status.html` |
| Sling Models and Apache Sling | `core/…/models/impl/OffersGridImpl.java`, `ApiStatusImpl.java`; `@Exporter` JSON |
| OSGi services and components | `core/…/integration/OfferApiClientImpl.java` (`@Component`, `@Designate`, `@Activate`) |
| Java and Maven | `core/pom.xml`, root `pom.xml` (archetype 58, Java 11 target) |
| Templates and Editable Templates | `ui.content/…/conf/aemproof/settings/wcm/templates/page-content`, `policies` |
| Author and Publish | `ui.config/…/config.author`, `config.publish`; dispatcher rules for publish |
| Content Fragments / Experience Fragments | `conf/…/dam/cfm/models/offer`, `content/dam/aemproof/offers`, `content/experience-fragments/aemproof/sa/ar/site/header` |
| Assets / DAM | `content/dam/aemproof/offers/images` referenced from fragments |
| Integrations with REST APIs | `OfferApiClient` + WireMock tests in `core/src/test/…/integration` |
| Git | Conventional Commits, ADRs in `docs/adr`, CI on every push |
| HTML, CSS, JavaScript | `ui.frontend/src/main/webpack` (SCSS tokens, component scripts) |
| AEM as a Cloud Service | `aemVersion=cloud`, `$[secret:…]` config, Cloud Service analyser in `all` |
| Cloud Manager, Dispatcher, CI/CD | `dispatcher/src/conf.dispatcher.d`, `.github/workflows/ci.yml`, ADR-0001 for the pipeline |

## Testing strategy

- **Sling Models** — wcm.io AEM Mocks (`AppAemContext`), JSON fixtures under `core/src/test/resources`.
- **Integration service** — WireMock on a random port: success, limit, HTTP 500, expired cache,
  timeout, malformed JSON, circuit open → half-open → closed, plus a pure state-machine test for
  the breaker with a controllable clock.
- **HTL** — there is no maintained HTL renderer for unit tests, so markup is proven two ways: the
  `htl-maven-plugin` validates every script at build time (warnings fail the build), and the Sling
  Models that feed the scripts are asserted field by field.
- **Coverage** — JaCoCo fails `core` under 80 % line coverage.
- **Cloud Service rules** — `aemanalyser-maven-plugin` in `all` runs on every `mvn verify`.

## Front-end

`ui.frontend` compiles SCSS + JS into `clientlib-site` (category `aemproof.site`), which AEM
serves at `/etc.clientlibs/aemproof/clientlibs/clientlib-site`. Fonts (Baloo Bhaijaan 2, IBM Plex Sans
Arabic, IBM Plex Mono; Arabic + Latin subsets, SIL OFL) are self-hosted in the clientlib's
`resources/fonts`. Right-to-left is switched by the page language (`html:lang(ar)`), not by a
template override. `src/main/webpack/static/index.html` mirrors the markup the components render
and is used for the static design preview (`npm start`, or `docs/PREVIEW.md` for a hosted build).
