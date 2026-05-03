#!/bin/bash

set -e

FILE="build/stack.tar"

if [ ! -f "$FILE" ]; then
  echo "❌ Missing $FILE"
  exit 1
fi

echo "📦 Loading Docker images..."
docker load -i "$FILE"
cp build/docker-compose.yml docker-compose.yml
cp build/nginx.conf nginx.conf
cp build/database.sql database.sql

echo "✅ Images loaded successfully"