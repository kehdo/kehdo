#!/usr/bin/env bash
#
# generate-openapi.sh — turn /contracts/openapi/kehdo.v1.yaml into Swift client
#
# Reads:
#   ../contracts/openapi/kehdo.v1.yaml
#
# Writes:
#   Packages/KHNetwork/Sources/KHNetwork/Generated/   (URLSession-based client)
#
# Run:
#   ./Scripts/generate-openapi.sh
#
# Uses Apple's swift-openapi-generator (preferred) or openapi-generator CLI.
# Phase 0 wires this into a Swift Package plugin so it runs on every build.

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SPEC="$ROOT/contracts/openapi/kehdo.v1.yaml"
OUT="$SCRIPT_DIR/../Packages/KHNetwork/Sources/KHNetwork/Generated"

if [ ! -f "$SPEC" ]; then
  echo "✗ Spec not found: $SPEC"
  exit 1
fi

echo "→ Generating Swift client from $SPEC"
echo "  (TODO: wire swift-openapi-generator)"
mkdir -p "$OUT"
echo "→ Output → $OUT"
echo "→ Done."
