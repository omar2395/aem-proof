# ADR 0001 — Target AEM as a Cloud Service, stay 6.5-compatible

Status: accepted · 2026-09-22

## Decision
Generate with `aemVersion=cloud`. Use only APIs present in `aem-sdk-api`. Read secrets via
`$[secret:…]` OSGi placeholders. Keep no 6.5-only constructs (no `/etc` client libraries, no
custom workflows in `/etc/workflow`).

## Consequences
- Deploys unchanged to Cloud Manager.
- For a 6.5 target: swap `aem-sdk-api` for `uber-jar`, add the Core Components dependency,
  replace `$[secret:…]` with plain OSGi config values. Nothing else changes.
