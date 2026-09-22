# Contributing / local setup

- JDK 17, Maven 3.9. Node and npm are downloaded by the `ui.frontend` module itself.
- Cold build (`mvn clean verify`, empty `~/.m2`): about 8 minutes. Warm: about 3 minutes.
- Java only: `mvn -pl core test`. Coverage report: `core/target/site/jacoco/index.html`.
- Front-end only: `cd ui.frontend && npm ci && npm run dev` (writes `clientlib-site` into `ui.apps`).
- Conventional Commits. One ADR per non-obvious decision in `docs/adr/`.
- Arabic is the primary content language. Every user-facing string goes through the i18n dictionary (`ui.apps/…/i18n/`).
