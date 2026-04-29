#!/usr/bin/env bash
#
# generate-tokens.sh — turn /design/tokens/*.json into Swift code
#
# Reads:
#   ../design/tokens/colors.json
#   ../design/tokens/typography.json
#
# Writes:
#   Packages/KHDesignSystem/Sources/KHDesignSystem/Generated/Colors.swift
#   Packages/KHDesignSystem/Sources/KHDesignSystem/Generated/Typography.swift
#
# Run:
#   ./Scripts/generate-tokens.sh
#
# Phase 0 will replace this stub with a real generator (Style Dictionary or
# a hand-rolled JSON-to-Swift transformer). For now it just signals intent.

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "→ Reading tokens from $ROOT/design/tokens/"
echo "  (TODO: implement generator)"
echo "  For now, AuroraColors.swift is hand-maintained as a placeholder."
echo "→ Done."
