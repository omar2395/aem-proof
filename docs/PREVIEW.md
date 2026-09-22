# Static design preview

AEM cannot run without a licensed organisation, so the visual preview is built from
`ui.frontend` alone: the real `clientlib-site` CSS/JS plus a static page whose markup mirrors what
the AEM components render for `/content/aemproof/sa/ar.html`.

## Locally

```bash
cd ui.frontend
npm ci
npm start            # webpack-dev-server on http://localhost:8080
```

Fonts resolve at the same path AEM serves them from (`/etc.clientlibs/aemproof/clientlibs/clientlib-site/resources/…`),
so what you see is the clientlib as it will ship.

## Hosted build

```bash
cd ui.frontend
npm ci
npm run preview      # writes a self-contained site to dist-preview/
```

Upload `dist-preview/` to any static host. It contains no AEM code, only the compiled clientlib,
fonts, the sample photographs and the mirrored markup.
