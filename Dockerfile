# Một image, một tiến trình (DECISIONS.md § D5).
#
# Node chỉ tồn tại ở stage build. Image cuối KHÔNG có Node runtime — đó là điểm cả D4 và
# PLAN.md § 2.1 nhấn: không SSR, không Node ở production.

# ── stage 1: build bundle Angular ─────────────────────────────────────────────
# Angular 22 đòi Node ^22.22.3 || ^24.15.0 || >=26 — khớp .tool-versions ở root.
FROM node:22.23-alpine AS frontend
WORKDIR /build
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build -- --configuration production

# ── stage 2: build jar, đã có bundle nằm trong static/app/ ────────────────────
FROM maven:3.9-eclipse-temurin-25 AS backend
WORKDIR /build
COPY backend/pom.xml ./
RUN mvn -B dependency:go-offline
COPY backend/src ./src
# Angular 22 (application builder) đẻ ra thêm một tầng browser/ — copy đúng tầng đó,
# nếu không index.html sẽ không nằm ở gốc static/app/ và fallback SPA fail âm thầm.
COPY --from=frontend /build/dist/frontend/browser/ ./src/main/resources/static/app/
RUN mvn -B -DskipTests package

# ── stage 3: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd -r checkino && useradd -r -g checkino checkino
COPY --from=backend /build/target/checkino-*.jar app.jar
USER checkino
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
