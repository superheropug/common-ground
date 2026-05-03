#!/bin/bash

set -e

OUT_DIR="build"
OUT_FILE="$OUT_DIR/stack.tar"

mkdir -p "$OUT_DIR"

echo "🔨 Building images..."
docker compose build

echo "📦 Extracting image list from compose..."
IMAGES=$(docker compose config | awk '/image:/ {print $2}')

if [ -z "$IMAGES" ]; then
  echo "❌ No images found in docker-compose config"
  exit 1
fi

echo "📦 Saving images:"
echo "$IMAGES"

docker save -o "$OUT_FILE" $IMAGES

echo "✅ Done: $OUT_FILE"