#!/bin/bash

set -e

echo "🧹 Stopping running containers..."
docker compose down --volumes --remove-orphans || true

echo "🧨 Removing project images..."

# Replace these with your actual image names if you tag them explicitly
docker image rm api frontend pg 2>/dev/null || true

echo "🧼 Removing dangling images..."
docker image prune -f

echo "🧽 Cleaning build cache..."
docker builder prune -f

echo "✅ Clean complete."
echo "ℹ️ You will need to rebuild images before running again."