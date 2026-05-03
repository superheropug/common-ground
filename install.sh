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

echo "✅ Images loaded successfully"