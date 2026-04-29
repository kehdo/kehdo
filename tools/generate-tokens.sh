#!/usr/bin/env bash
# ============================================================================
# generate-tokens.sh — turn design/tokens/*.json into native code
# ============================================================================
# Outputs (all gitignored — regenerated each build):
#   android/core/core-ui/build/generated/tokens/Color.kt
#   ios/Packages/KHDesignSystem/Sources/Tokens/Generated/Colors.swift
#   web/src/styles/tokens.generated.css
# ============================================================================

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT_DIR="$( cd "$SCRIPT_DIR/.." &> /dev/null && pwd )"

TOKENS_DIR="$ROOT_DIR/design/tokens"

PURPLE='\033[1;35m'
GREEN='\033[1;32m'
NC='\033[0m'

echo -e "${PURPLE}▸ Generating design tokens${NC}"

if [ ! -f "$TOKENS_DIR/colors.json" ]; then
    echo "  ⚠ design/tokens/colors.json not found — nothing to generate"
    exit 0
fi

# This script is a placeholder. The real implementation should:
#   1. Parse colors.json with jq or a small Node/Python script
#   2. Emit Kotlin Color.kt, Swift Colors.swift, CSS custom props
#   3. Emit typography Type scale equivalents
#
# For now, this stub creates output directories so first builds don't fail.

mkdir -p "$ROOT_DIR/android/core/core-ui/build/generated/tokens"
mkdir -p "$ROOT_DIR/ios/Packages/KHDesignSystem/Sources/Tokens/Generated"
mkdir -p "$ROOT_DIR/web/src/styles"

# Placeholder Kotlin file
cat > "$ROOT_DIR/android/core/core-ui/build/generated/tokens/Color.kt" <<'KT'
// AUTO-GENERATED FROM design/tokens/colors.json — DO NOT EDIT
package app.kehdo.android.core.ui

import androidx.compose.ui.graphics.Color

object AuroraColors {
    val Bg = Color(0xFF0A0612)
    val Surface = Color(0xFF1A0F2E)
    val Purple = Color(0xFF9C5BFF)
    val PurpleBright = Color(0xFFB47BFF)
    val Pink = Color(0xFFEC4899)
    val Amber = Color(0xFFF59E0B)
    val Blue = Color(0xFF3B82F6)
    val Text = Color(0xFFF5F3FF)
}
KT

# Placeholder Swift file
cat > "$ROOT_DIR/ios/Packages/KHDesignSystem/Sources/Tokens/Generated/Colors.swift" <<'SW'
// AUTO-GENERATED FROM design/tokens/colors.json — DO NOT EDIT
import SwiftUI

public enum AuroraColors {
    public static let bg = Color(red: 0x0A/255, green: 0x06/255, blue: 0x12/255)
    public static let surface = Color(red: 0x1A/255, green: 0x0F/255, blue: 0x2E/255)
    public static let purple = Color(red: 0x9C/255, green: 0x5B/255, blue: 0xFF/255)
    public static let purpleBright = Color(red: 0xB4/255, green: 0x7B/255, blue: 0xFF/255)
    public static let pink = Color(red: 0xEC/255, green: 0x48/255, blue: 0x99/255)
    public static let amber = Color(red: 0xF5/255, green: 0x9E/255, blue: 0x0B/255)
    public static let blue = Color(red: 0x3B/255, green: 0x82/255, blue: 0xF6/255)
    public static let text = Color(red: 0xF5/255, green: 0xF3/255, blue: 0xFF/255)
}
SW

# Placeholder CSS file
cat > "$ROOT_DIR/web/src/styles/tokens.generated.css" <<'CSS'
/* AUTO-GENERATED FROM design/tokens/colors.json — DO NOT EDIT */
:root {
  --color-bg: #0A0612;
  --color-bg-2: #120A1F;
  --color-surface: #1A0F2E;
  --color-surface-2: #24173D;
  --color-purple: #9C5BFF;
  --color-purple-bright: #B47BFF;
  --color-purple-deep: #6B2FD9;
  --color-pink: #EC4899;
  --color-amber: #F59E0B;
  --color-blue: #3B82F6;
  --color-text: #F5F3FF;
  --color-text-dim: rgba(245, 243, 255, 0.65);
  --color-text-mute: rgba(245, 243, 255, 0.45);
  --color-line: rgba(255, 255, 255, 0.08);
  --color-success: #10B981;

  --gradient-aurora: linear-gradient(135deg, #9C5BFF 0%, #EC4899 50%, #F59E0B 100%);
  --gradient-glow: linear-gradient(135deg, #9C5BFF 0%, #3B82F6 100%);
}
CSS

echo -e "  ${GREEN}✓${NC} Tokens generated for Android, iOS, Web"
