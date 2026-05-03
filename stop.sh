#!/bin/bash

set -e

echo "🛑 Stopping stack..."
docker compose down

echo "✅ Stopped"