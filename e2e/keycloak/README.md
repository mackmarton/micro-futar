# Tesztrealm – az éles Keycloakba importálni TILOS

A `micro-futar-e2e-realm.json` **kizárólag a teszt- és fejlesztőkörnyezeté** (docker compose). Éles Keycloakba
importálni tilos: a kliens titka, a felhasználók és a jelszavaik nyilvánosak, a redirect URI-k `localhost`-ra mutatnak,
a levelek a Mailpitbe mennek.

- Az éles realm részleges exportjából készült (kulcsok nélkül; a Keycloak importkor generál sajátot).
- A kliens titka az `E2E_CLIENT_SECRET` környezeti változóból jön (a Keycloak az importkor helyettesíti be).
- A realm minden Keycloak-indításkor tiszta, beágyazott adatbázisba töltődik be.

| Felhasználó | Jelszó (eldobható) | `api-gateway-client` szerepkörök |
|---|---|---|
| `sender@e2e.test` | `e2e-sender-pass` | – |
| `dispatcher@e2e.test` | `e2e-dispatcher-pass` | `logistics-user`, `admin` |
| `courier@e2e.test` | `e2e-courier-pass` | `courier-user` |
