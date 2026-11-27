# E2E futtatási környezet

A teljes rendszer docker compose-ban: infra, a hat Spring-szolgáltatás, a három UI, a saját Keycloak-témát tartalmazó
Keycloak a tesztrealmmel, valamint az OpenRouteService és a Stripe helyettesítői. Csak teszt- és fejlesztőkörnyezet.

## Előfeltételek

- Docker Desktop (Compose v2), a Docker VM-nek **legalább 10 GB** memória (a stack kb. 6 GB-ot használ, a build többet).
- Node 22+ a seed scripthez.
- A `keycloak` hostnév a gépen. A böngésző és a konténerek ugyanazon a címen (`http://keycloak:8180`) érik el a
  Keycloakot, ezért egyezik a tokenek kibocsátója (`iss`). Egyszer kell felvenni:

```bash
echo "127.0.0.1 keycloak" | sudo tee -a /etc/hosts
```

## Indítás

A repó gyökerében:

```bash
docker compose --profile apps up -d --build --wait
```

```bash
node e2e/scripts/seed.mjs
```

A `--wait` akkor tér vissza, amikor minden konténer healthy. A seed a futár felhasználóhoz tartozó futár-rekordot
hozza létre (enélkül a `/api/courier/me` 404); többször is futtatható.

Ha kevés a memória a párhuzamos buildhez, építsd sorban:

```bash
for s in orders logistics courier tracking payments api_gateway client-ui logistics-ui courier-ui keycloak; do docker compose --profile apps build $s || break; done
```

### Ha már volt helyi adatbázisod

A Postgres kötet megmarad az indítások között, és a felhasználóit az **első** indításkor kapja. Ha korábban más
`.env`-vel indítottad (jelszóhiba vagy hiányzó adatbázis a logokban), indítsd külön projektnéven, saját üres kötettel;
a régi kötet érintetlen marad:

```bash
docker compose -p micro-futar-e2e --profile apps up -d --build --wait
```

## Címek

| Mi | Cím |
|---|---|
| client-ui (feladó, követés) | http://localhost:5173 |
| logistics-ui (diszpécser) | http://localhost:5174 |
| courier-ui (futár) | http://localhost:5175 |
| API gateway | http://localhost:8085 |
| Keycloak (admin: `admin` / `e2e-admin-pass`) | http://keycloak:8180 |
| Mailpit | http://localhost:8025 |
| ORS-mock | http://localhost:12110 |
| Stripe-mock | http://localhost:12111 |
| orders, logistics, courier, tracking, payments | 8081, 8082, 8083, 8084, 8086 |

## Tesztfelhasználók

| Felhasználó | Jelszó | Szerepkörök |
|---|---|---|
| `sender@e2e.test` | `e2e-sender-pass` | – |
| `dispatcher@e2e.test` | `e2e-dispatcher-pass` | `logistics-user`, `admin` |
| `courier@e2e.test` | `e2e-courier-pass` | `courier-user` |

Lásd még: [keycloak/README.md](keycloak/README.md).

## Ellenőrzés

Mindhárom felhasználó bejelentkezik a gatewayen át, és lekéri a saját adatait:

```bash
node e2e/scripts/seed.mjs --check
```

## Mockok

- **ORS** (`mocks/ors`): geokódolás Budapest, Győr, Debrecen, Bécs/Wien, Graz és Linz városokra, más névre üres
  találat. A mátrix a koordinátákból becsül menetidőt (észak–déli és kelet–nyugati távolság, 60 km/h). A geokódoló stubokat a
  `mocks/generate-ors-geocode.py` írja.
- **Stripe** (`mocks/stripe`): checkout session létrehozás és lejáratás, payment intent capture (a kért összeget adja
  vissza) és cancel. Nem tart állapotot. A valódi Checkout oldal helyett egy statikus oldal jelenik meg.
- **Stripe webhook:** a teszt küldi a `POST http://localhost:8085/api/payments/webhooks/stripe` címre, a
  `whsec_e2e_test_only` titokkal aláírva (`Stripe-Signature` fejléc).
  Az eseményben legyen `api_version` mező (pl. `"2025-01-27.acacia"`), különben a `payments` 500-zal válaszol.
- A UI-k a böngészőből közvetlenül hívják a Nominatimot és a térképcsempéket; ezeket nem mockolja a compose.

## Környezeti változók

Mindegyiknek van tesztértéke, `.env` nem kötelező. Felülírható: `POSTGRES_USER`, `POSTGRES_PASSWORD`,
`KEYCLOAK_ADMIN_PASSWORD`, `E2E_CLIENT_SECRET`, `STRIPE_API_BASE`, `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`.
Valódi teszt módú Stripe-hoz: `STRIPE_API_BASE=https://api.stripe.com` és a saját kulcsaid.

## Fejlesztés a gépen futó szolgáltatással

A compose Keycloakja már nem a `localhost:8090`-en fut. A gépen (`mvn spring-boot:run`) futtatott szolgáltatásnak:

- `KEYCLOAK_URL=http://keycloak:8180`
- `KEYCLOAK_ISSUER_URI=http://keycloak:8180/realms/micro-futar-realm`
- gatewaynél `CLIENT_SECRET=e2e-only-client-secret`

A Keycloak beágyazott adatbázisa nem marad meg: a konzolon végzett módosítás újraindításkor elvész. A `keycloak_db`-ben
lévő korábbi helyi realmedhez a compose nem nyúl, de nem is használja.

## Leállítás

```bash
docker compose --profile apps down
```

A `down -v` az adatbázist is törli.
