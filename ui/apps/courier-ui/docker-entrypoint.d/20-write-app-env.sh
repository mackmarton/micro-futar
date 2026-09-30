#!/bin/sh
# A nginx image automatikusan lefuttatja a /docker-entrypoint.d/*.sh
# scripteket a szerver elindítása előtt. Itt írjuk felül a build-időben
# generált (üres) app-env.js-t a konténer futásidejű env változóival.
set -eu

cat > /usr/share/nginx/html/app-env.js <<EOF
window.__APP_ENV__ = window.__APP_ENV__ || {};
window.__APP_ENV__.VITE_API_BASE_URL = "${VITE_API_BASE_URL:-}";
EOF
