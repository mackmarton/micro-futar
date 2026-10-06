# Stage 1: install workspace deps and build the client-ui app
# Build context is the `ui/` directory (npm workspaces root), not the repo root.
FROM node:22-alpine AS builder
WORKDIR /build
COPY . .
RUN npm ci
RUN npm run build --workspace=apps/client-ui

# Stage 2: serve the static Next.js export (`output: 'export'`) with nginx
FROM nginx:1.27-alpine
COPY --from=builder /build/apps/client-ui/out /usr/share/nginx/html
COPY apps/client-ui/nginx.conf /etc/nginx/conf.d/default.conf
COPY apps/client-ui/docker-entrypoint.d/20-write-app-env.sh /docker-entrypoint.d/20-write-app-env.sh
RUN chmod +x /docker-entrypoint.d/20-write-app-env.sh
EXPOSE 80
