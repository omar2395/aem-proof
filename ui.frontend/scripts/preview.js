/**
 * Builds a self-contained static preview in dist-preview/:
 *   - the compiled clientlib-site CSS/JS at the path AEM serves it from,
 *   - the fonts under .../clientlib-site/resources/fonts (same URL as in AEM),
 *   - the static mock page and its sample photographs.
 * No AEM code is involved; this is the front-end as it will ship, viewable anywhere.
 */
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const dist = path.join(root, 'dist', 'clientlib-site');
const staticDir = path.join(root, 'src', 'main', 'webpack', 'static');
const out = path.join(root, 'dist-preview');
const clientlibOut = path.join(out, 'etc.clientlibs', 'aemproof', 'clientlibs', 'clientlib-site');

fs.rmSync(out, { recursive: true, force: true });
fs.mkdirSync(clientlibOut, { recursive: true });

// CSS/JS live at /etc.clientlibs/aemproof/clientlibs/clientlib-site.css|.js in AEM; keep the same URLs.
fs.copyFileSync(path.join(dist, 'site.css'), path.join(clientlibOut, '..', 'clientlib-site.css'));
fs.copyFileSync(path.join(dist, 'site.js'), path.join(clientlibOut, '..', 'clientlib-site.js'));
fs.cpSync(path.join(dist, 'fonts'), path.join(clientlibOut, 'resources', 'fonts'), { recursive: true });
fs.cpSync(path.join(staticDir, 'assets'), path.join(out, 'assets'), { recursive: true });

let html = fs.readFileSync(path.join(staticDir, 'index.html'), 'utf8');
html = html.replace('</head>',
    '    <link rel="stylesheet" href="/etc.clientlibs/aemproof/clientlibs/clientlib-site.css">\n</head>')
    .replace('</body>',
    '    <script src="/etc.clientlibs/aemproof/clientlibs/clientlib-site.js"></script>\n</body>');
fs.writeFileSync(path.join(out, 'index.html'), html);

console.log('preview written to ' + out);
