# Stage 1: install workspace deps and build the courier-ui app
# Build context is the `ui/` directory (npm workspaces root), not the repo root.
FROM node:22-alpine AS builder
WORKDIR /build
COPY . .
RUN npm ci
RUN npm run build --workspace=apps/courier-ui

# Stage 2: serve the static build with nginx
FROM nginx:1.27-alpine
COPY --from=builder /build/apps/courier-ui/dist /usr/share/nginx/html
COPY apps/courier-ui/nginx.conf /etc/nginx/conf.d/default.conf
COPY apps/courier-ui/docker-entrypoint.d/20-write-app-env.sh /docker-entrypoint.d/20-write-app-env.sh
RUN chmod +x /docker-entrypoint.d/20-write-app-env.sh
EXPOSE 80
