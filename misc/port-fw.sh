#!/bin/bash

# Port-forwardok indítása a háttérben
kubectl port-forward -n argocd svc/argocd-server 8081:443 &
kubectl port-forward -n vault vault-0 8200:8200 &
kubectl port-forward -n micro-futar-services-dev svc/keycloak 8090:80 &
kubectl port-forward -n micro-futar-services-dev svc/traefik 8070:80 &
kubectl port-forward -n micro-futar-services-dev svc/postgres-db-postgresql 6432:5432 &

# Amikor leállítod a scriptet (Ctrl+C), lője le a háttérfolyamatokat is
trap "kill 0" EXIT

# Várakozás, hogy ne lépjen ki a script
wait