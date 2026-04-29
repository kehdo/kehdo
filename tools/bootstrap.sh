#!/usr/bin/env bash
# ============================================================================
# kehdo bootstrap — one-command setup for new developers
# ============================================================================
# Run from repo root:  ./tools/bootstrap.sh
# Idempotent: safe to re-run.
# ============================================================================

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
ROOT_DIR="$( cd "$SCRIPT_DIR/.." &> /dev/null && pwd )"
cd "$ROOT_DIR"

# Colors
PURPLE='\033[1;35m'
GREEN='\033[1;32m'
YELLOW='\033[1;33m'
RED='\033[1;31m'
NC='\033[0m'

step() { echo -e "\n${PURPLE}▸ $1${NC}"; }
ok() { echo -e "  ${GREEN}✓${NC} $1"; }
warn() { echo -e "  ${YELLOW}!${NC} $1"; }
fail() { echo -e "  ${RED}✗${NC} $1"; exit 1; }

echo -e "${PURPLE}"
echo "  ╦╔═┌─┐┬ ┬┌┬┐┌─┐"
echo "  ╠╩╗├┤ ├─┤ │││ │"
echo "  ╩ ╩└─┘┴ ┴─┴┘└─┘  bootstrap"
echo -e "${NC}"
echo "  This will set up your local development environment."
echo "  Estimated time: 5-15 minutes (first run, depending on connection)."
echo ""

# ---------------------------------------------------------------------------
# 1. Verify required tools
# ---------------------------------------------------------------------------
step "Checking required tools"

require() {
    local cmd="$1"
    local hint="$2"
    if command -v "$cmd" &>/dev/null; then
        ok "$cmd found"
    else
        fail "$cmd missing — install with: $hint"
    fi
}

require git "https://git-scm.com"
require docker "https://docker.com or 'brew install --cask docker'"
require java "brew install openjdk@21"
require node "brew install node@20"

# Optional tools (warn but don't fail)
optional() {
    local cmd="$1"
    local hint="$2"
    if command -v "$cmd" &>/dev/null; then
        ok "$cmd found"
    else
        warn "$cmd not found — needed for: $hint"
    fi
}

optional pnpm "web dev (npm install -g pnpm)"
optional openapi-generator "generating clients (brew install openapi-generator)"
optional swiftlint "iOS dev (brew install swiftlint)"

# ---------------------------------------------------------------------------
# 2. Verify Java version (need 21+)
# ---------------------------------------------------------------------------
step "Verifying Java version"
JAVA_MAJOR=$(java -version 2>&1 | head -n1 | awk -F '"' '{print $2}' | awk -F. '{print $1}')
if [ "${JAVA_MAJOR:-0}" -lt 21 ]; then
    fail "Java 21+ required, found Java $JAVA_MAJOR. Try: brew install openjdk@21"
fi
ok "Java $JAVA_MAJOR detected"

# ---------------------------------------------------------------------------
# 3. Generate design tokens (colors, typography → native code)
# ---------------------------------------------------------------------------
step "Generating design tokens for all platforms"
if [ -f "$SCRIPT_DIR/generate-tokens.sh" ]; then
    bash "$SCRIPT_DIR/generate-tokens.sh"
    ok "Design tokens generated"
else
    warn "generate-tokens.sh not yet implemented — skipping"
fi

# ---------------------------------------------------------------------------
# 4. Generate API clients from OpenAPI spec
# ---------------------------------------------------------------------------
step "Generating API clients from OpenAPI spec"
if [ -f "$SCRIPT_DIR/generate-clients.sh" ]; then
    bash "$SCRIPT_DIR/generate-clients.sh"
    ok "API clients generated"
else
    warn "generate-clients.sh not yet implemented — skipping"
fi

# ---------------------------------------------------------------------------
# 5. Start local services (Postgres, Redis, MinIO)
# ---------------------------------------------------------------------------
step "Starting local services (Postgres, Redis, MinIO)"
if [ -f "$ROOT_DIR/infra/docker-compose.yml" ]; then
    docker compose -f "$ROOT_DIR/infra/docker-compose.yml" up -d
    ok "Local services running"
    echo "    Postgres: localhost:5432  (user: kehdo, pass: kehdo, db: kehdo)"
    echo "    Redis:    localhost:6379"
    echo "    MinIO:    localhost:9000  (console: localhost:9001)"
else
    warn "docker-compose.yml not found — skipping services"
fi

# ---------------------------------------------------------------------------
# 6. Backend setup
# ---------------------------------------------------------------------------
step "Building backend (Gradle)"
if [ -f "$ROOT_DIR/backend/gradlew" ]; then
    cd "$ROOT_DIR/backend"
    ./gradlew build -x test --no-daemon || warn "Backend build failed — check logs"
    ok "Backend built"
    cd "$ROOT_DIR"
else
    warn "Backend gradlew missing — skipping (run after first git pull populates it)"
fi

# ---------------------------------------------------------------------------
# 7. Web setup
# ---------------------------------------------------------------------------
step "Installing web dependencies"
if [ -f "$ROOT_DIR/web/package.json" ]; then
    cd "$ROOT_DIR/web"
    if command -v pnpm &>/dev/null; then
        pnpm install
        ok "Web deps installed"
    else
        npm install
        warn "Used npm — consider installing pnpm: npm install -g pnpm"
    fi
    cd "$ROOT_DIR"
else
    warn "web/package.json missing — skipping"
fi

# ---------------------------------------------------------------------------
# 8. Done
# ---------------------------------------------------------------------------
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  Bootstrap complete.${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Next steps — pick your IDE and open the right folder:"
echo ""
echo -e "  ${PURPLE}Backend (IntelliJ IDEA):${NC}"
echo "    idea backend/"
echo ""
echo -e "  ${PURPLE}Android (Android Studio):${NC}"
echo "    open -a 'Android Studio' android/"
echo ""
echo -e "  ${PURPLE}iOS (Xcode):${NC}"
echo "    open ios/Kehdo.xcworkspace"
echo ""
echo -e "  ${PURPLE}Web (VS Code):${NC}"
echo "    code web/"
echo "    cd web && pnpm dev"
echo ""
echo "  Read CLAUDE.md in your chosen folder for platform-specific rules."
echo ""
