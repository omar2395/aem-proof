# Architecture

## Modules

| Module | Responsibility |
|---|---|
| `core` | Java: Sling Models (`com.aemproof.core.models`) and OSGi services (`com.aemproof.core.integration`). |
| `ui.apps` | Components (dialog XML + HTL), client libraries, i18n dictionary. |
| `ui.content` | Templates, policies, Content Fragment model, Experience Fragments, sample content. |
| `ui.config` | OSGi configuration per run mode. |
| `ui.frontend` | Webpack build producing `clientlib-site`; also hosts the static design preview. |
| `ui.apps.structure` | Repository structure package (Cloud Service requirement). |
| `it.tests` | Integration tests run against a real instance (Cloud Manager). |
| `dispatcher` | Cloud Service dispatcher configuration. |
| `all` | The single deployable content package. |

## Request flow

_Filled in once the components exist (see the changelog)._
