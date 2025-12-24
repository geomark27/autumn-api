#!/bin/bash

# Script para levantar la infraestructura local del proyecto

echo "🚀 Iniciando infraestructura de Autumn Banking System..."

# PostgreSQL
echo "📊 Levantando PostgreSQL..."
docker run --name autumn-postgres \
  -e POSTGRES_DB=autumn_dev \
  -e POSTGRES_USER=autumn_user \
  -e POSTGRES_PASSWORD=autumn_pass \
  -p 5432:5432 \
  -d postgres:16

# Redis
echo "🔴 Levantando Redis..."
docker run --name autumn-redis \
  -p 6379:6379 \
  -d redis:7-alpine

echo "✅ Infraestructura lista!"
echo ""
echo "Servicios disponibles:"
echo "  - PostgreSQL: localhost:5432 (DB: autumn_dev, User: autumn_user)"
echo "  - Redis: localhost:6379"
echo ""
echo "Para detener:"
echo "  docker stop autumn-postgres autumn-redis"
echo ""
echo "Para eliminar:"
echo "  docker rm autumn-postgres autumn-redis"
