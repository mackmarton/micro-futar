# Stage 1: build the Keycloakify provider jar
# Build context is the `ui/` directory (npm workspaces root), not the repo root.
FROM node:22-alpine AS builder
WORKDIR /build
COPY . .
RUN npm ci
RUN npm run build-keycloak-theme --workspace=apps/keycloak-theme

# Stage 2: bake the theme into the same Keycloak base image used by
# misc/helm/keycloak-values.yaml. Keycloak 26.x falls into Keycloakify's
# "all other versions" jar bucket (it only ships a dedicated jar for 22-25).
FROM bitnamilegacy/keycloak:26.3.3-debian-12-r0
COPY --from=builder /build/apps/keycloak-theme/dist_keycloak/keycloak-theme-for-kc-all-other-versions.jar /opt/bitnami/keycloak/providers/
RUN /opt/bitnami/keycloak/bin/kc.sh build
