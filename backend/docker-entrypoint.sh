#!/bin/sh
# ============================================================
# kehdo backend — container entrypoint
# ============================================================
# Materialises the GCP service-account JSON from a Fly secret to
# /data/gcp-credentials.json on every container start, then execs
# the JVM. Avoids the SSH-into-running-machine dance that was
# previously needed to seed the volume.
#
# Required env var on the hosted env:
#   KEHDO_GCP_CREDENTIALS_B64 — base64-encoded service-account JSON.
#                               Set via `fly secrets set` (one-time).
#                               Base64 sidesteps newline/quoting issues
#                               that plain JSON-as-env-var hits.
#
# Local dev (no /data volume, no env var) just falls through to the
# JVM exec — Spring's GCP creds path is configured per-profile.
# ============================================================
set -eu

CREDS_PATH="${GOOGLE_APPLICATION_CREDENTIALS:-/data/gcp-credentials.json}"

if [ -n "${KEHDO_GCP_CREDENTIALS_B64:-}" ]; then
    creds_dir="$(dirname "$CREDS_PATH")"
    mkdir -p "$creds_dir"
    # busybox base64 has no -d flag in some Alpine versions; -d works
    # on coreutils. Fall back to --decode if needed.
    if echo "" | base64 -d > /dev/null 2>&1; then
        echo "$KEHDO_GCP_CREDENTIALS_B64" | base64 -d > "$CREDS_PATH"
    else
        echo "$KEHDO_GCP_CREDENTIALS_B64" | base64 --decode > "$CREDS_PATH"
    fi
    chmod 600 "$CREDS_PATH"
    # Always export GOOGLE_APPLICATION_CREDENTIALS for the JVM. Without
    # this, Google's Application Default Credentials chain falls past
    # the file-path step and tries the GCE metadata server, which Fly
    # doesn't have, producing
    #   io.grpc.StatusRuntimeException:
    #     UNAVAILABLE: Credentials failed to obtain metadata
    # from Cloud Vision and Vertex AI calls. Exporting the path here
    # guarantees the JVM sees the same value regardless of whether the
    # GOOGLE_APPLICATION_CREDENTIALS fly secret was set explicitly.
    export GOOGLE_APPLICATION_CREDENTIALS="$CREDS_PATH"
    echo "[entrypoint] GCP credentials materialised at $CREDS_PATH"
    echo "[entrypoint] GOOGLE_APPLICATION_CREDENTIALS exported for JVM"
else
    echo "[entrypoint] KEHDO_GCP_CREDENTIALS_B64 not set; assuming local-dev or stub-mode AI"
fi

exec java \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+HeapDumpOnOutOfMemoryError \
    -jar /app/kehdo.jar "$@"
