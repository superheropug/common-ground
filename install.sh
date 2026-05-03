#!/bin/bash

set -e

FILE="build/stack.tar"

if [ ! -f "$FILE" ]; then
  echo "❌ Missing $FILE"
  exit 1
fi

echo "📦 Loading Docker images..."
docker load -i "$FILE"

echo "✅ Images loaded successfully"