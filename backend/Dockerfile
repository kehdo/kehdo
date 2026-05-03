# ============================================================
# kehdo backend — multi-stage build
# ============================================================

# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY app ./app
COPY api ./api
COPY auth ./auth
COPY user ./user
COPY conversation ./conversation
COPY ai ./ai
COPY infra ./infra
COPY common ./common
COPY contracts-openapi ./contracts-openapi

# Strip CRLF from gradlew before exec — Windows clones with
# core.autocrlf=true commit LF but check out CRLF, and Alpine's sh can't
# run a script whose shebang ends with \r (kernel looks for an
# interpreter named "sh\r" and fails with a misleading "not found").
RUN sed -i 's/\r$//' gradlew && chmod +x ./gradlew && ./gradlew :app:bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S kehdo && adduser -S kehdo -G kehdo
USER kehdo

COPY --from=builder /build/app/build/libs/*.jar /app/kehdo.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
    CMD wget -q -O - http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-jar", "/app/kehdo.jar"]
