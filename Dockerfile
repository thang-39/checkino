# Một image, một tiến trình (DECISIONS.md § D5).
#
# Node chỉ tồn tại ở stage build. Image cuối KHÔNG có Node runtime — đó là điểm cả D4 và
# PLAN.md § 2.1 nhấn: không SSR, không Node ở production.

# ── stage 1: build bundle Angular ─────────────────────────────────────────────
# Angular 22 đòi Node ^22.22.3 || ^24.15.0 || >=26 — khớp .tool-versions ở root.
FROM node:22.23-alpine AS frontend
WORKDIR /build
# CA MITM công ty (tuỳ chọn): có ở máy dev sau TLS interception, KHÔNG có ở CI/máy sạch.
# certs/ luôn có .gitkeep nên COPY không vỡ; cert thật bị .gitignore, không commit.
# Gộp mọi *.crt (nếu có) thành một file cho NODE_EXTRA_CA_CERTS; không có thì để file rỗng → no-op.
COPY certs/ /tmp/certs/
RUN if ls /tmp/certs/*.crt >/dev/null 2>&1; then cat /tmp/certs/*.crt > /tmp/company-ca.crt; else : > /tmp/company-ca.crt; fi
ENV NODE_EXTRA_CA_CERTS=/tmp/company-ca.crt
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build -- --configuration production

# ── stage 2: build jar, đã có bundle nằm trong static/app/ ────────────────────
FROM maven:3.9-eclipse-temurin-25 AS backend
WORKDIR /build
# CA MITM công ty (tuỳ chọn — xem stage frontend). Java KHÔNG đọc trust store hệ điều hành, nên
# import thẳng vào cacerts của JDK để Maven gọi HTTPS ra Maven Central không lỗi PKIX.
# Không có cert (CI/máy sạch) → vòng lặp rỗng, bỏ qua.
COPY certs/ /tmp/certs/
RUN if ls /tmp/certs/*.crt >/dev/null 2>&1; then \
      for c in /tmp/certs/*.crt; do \
        keytool -importcert -noprompt -trustcacerts \
          -alias "company-$(basename "$c" .crt)" \
          -file "$c" \
          -keystore "$JAVA_HOME/lib/security/cacerts" \
          -storepass changeit; \
      done; \
    fi
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
