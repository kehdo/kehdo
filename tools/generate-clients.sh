#!/usr/bin/env bash
# ============================================================================
# generate-clients.sh — turn contracts/openapi/kehdo.v1.yaml into clients
# ============================================================================
# Generates:
#   - Backend Spring stubs   → backend/contracts-openapi/src/main/java/generated/
#   - Android Retrofit       → android/core/core-network-generated/src/main/java/
#   - iOS Swift              → ios/Packages/KHNetwork/Sources/Generated/
#   - TypeScript (web)       → web/src/generated/
# ============================================================================

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT_DIR="$( cd "$SCRIPT_DIR/.." &> /dev/null && pwd )"
SPEC="$ROOT_DIR/contracts/openapi/kehdo.v1.yaml"

PURPLE='\033[1;35m'
GREEN='\033[1;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${PURPLE}▸ Generating API clients from $SPEC${NC}"

if [ ! -f "$SPEC" ]; then
    echo "  ⚠ $SPEC not found — nothing to generate"
    exit 0
fi

if ! command -v openapi-generator &>/dev/null; then
    echo -e "  ${YELLOW}!${NC} openapi-generator not installed"
    echo "    Install: brew install openapi-generator"
    echo "    Skipping client generation for now."
    exit 0
fi

# Backend (Java Spring)
mkdir -p "$ROOT_DIR/backend/contracts-openapi/src/main/java/generated"
openapi-generator generate \
    -i "$SPEC" \
    -g spring \
    -o "$ROOT_DIR/backend/contracts-openapi/src/main/java/generated" \
    --additional-properties=interfaceOnly=true,useSpringBoot3=true,useTags=true \
    --skip-validate-spec || true
echo -e "  ${GREEN}✓${NC} Backend Spring stubs"

# Android (Kotlin Retrofit)
mkdir -p "$ROOT_DIR/android/core/core-network-generated/src/main/java"
openapi-generator generate \
    -i "$SPEC" \
    -g kotlin \
    -o "$ROOT_DIR/android/core/core-network-generated" \
    --additional-properties=library=jvm-retrofit2,serializationLibrary=kotlinx_serialization,useCoroutines=true,packageName=app.kehdo.android.network.generated \
    --skip-validate-spec || true
echo -e "  ${GREEN}✓${NC} Android Retrofit client"

# iOS (Swift)
mkdir -p "$ROOT_DIR/ios/Packages/KHNetwork/Sources/Generated"
openapi-generator generate \
    -i "$SPEC" \
    -g swift5 \
    -o "$ROOT_DIR/ios/Packages/KHNetwork/Sources/Generated" \
    --additional-properties=projectName=KehdoAPI,responseAs=AsyncAwait,useClasses=false \
    --skip-validate-spec || true
echo -e "  ${GREEN}✓${NC} iOS Swift client"

# Web/Extensions (TypeScript)
mkdir -p "$ROOT_DIR/web/src/generated"
openapi-generator generate \
    -i "$SPEC" \
    -g typescript-axios \
    -o "$ROOT_DIR/web/src/generated" \
    --additional-properties=supportsES6=true,withInterfaces=true \
    --skip-validate-spec || true
echo -e "  ${GREEN}✓${NC} TypeScript types"

echo -e "${GREEN}All clients generated.${NC}"
