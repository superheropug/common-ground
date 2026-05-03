#!/bin/bash

set -e

echo "🚀 Starting stack..."
docker compose up -d

echo "✅ Running"
docker compose ps