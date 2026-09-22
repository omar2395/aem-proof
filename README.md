# AEM Proof

[![CI](https://github.com/omar2395/aem-proof/actions/workflows/ci.yml/badge.svg)](https://github.com/omar2395/aem-proof/actions/workflows/ci.yml)

A small, complete Adobe Experience Manager (AEM as a Cloud Service) project. It exists to show, in code you can clone and build, how I structure AEM components, Sling Models, OSGi services and external integrations.

**العربية:** [README.ar.md](README.ar.md)

## What is in here

| Proof | Where |
|---|---|
| Custom component with authoring dialog, HTL and Sling Model | `ui.apps/…/components/offers-grid`, `core/…/models/impl/OffersGridImpl.java` |
| OSGi integration service with config, timeout, cache and circuit breaker | `core/…/integration/` |
| Content Fragment model + Arabic sample fragments | `ui.content/…/conf/aemproof/settings/dam/cfm/models/offer` |
| Experience Fragment header locked into an Editable Template | `ui.content/…/experience-fragments/aemproof/sa/ar/site/header`, `conf/aemproof/settings/wcm/templates/page-content` |
| Run-mode OSGi configuration (dev / prod) | `ui.config/…/osgiconfig/` |
| Dispatcher filters and cache rules | `dispatcher/src/conf.dispatcher.d/` |
| Unit tests: AEM Mocks + WireMock, 80 % coverage gate | `core/src/test/` |
| Front-end build (the client library AEM serves) with a static design preview | `ui.frontend/` |

## Build

Requires JDK 17 and Maven 3.9. No Adobe account is needed to build and run the tests.

```bash
mvn clean verify
```

## Deploy to an AEM instance

```bash
mvn clean install -PautoInstallSinglePackage            # author on :4502
mvn clean install -PautoInstallSinglePackagePublish     # publish on :4503
```

Or push the repo to a Cloud Manager Git remote; the project already follows the Cloud Service layout.

## Testing

`mvn -pl core verify` runs the Sling Model tests (AEM Mocks) and the integration tests (WireMock),
then fails the build under 80 % line coverage. HTL scripts are validated by `htl-maven-plugin`,
and the AEM Cloud Service analyser checks the final package. Details in
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md#testing-strategy).

## Design preview

The front-end is a real client library with a static preview page, so the design can be seen
without an AEM instance: [docs/PREVIEW.md](docs/PREVIEW.md).

## Cloud Service or 6.5

Generated for AEM as a Cloud Service; the only 6.5 changes are the API dependency and the secret
placeholders, listed in [ADR-0001](docs/adr/0001-cloud-service-target.md).

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Decision records](docs/adr/)
- [Contributing / local setup](docs/CONTRIBUTING.md)
- [Static design preview](docs/PREVIEW.md)
- [Changelog](CHANGELOG.md)

## A note on running it

AEM cannot run without a licensed Adobe organisation, so this repo ships code and tests, not a public AEM URL. Invite me to your Adobe Admin Console and it is deployed to your development environment within the hour.
